/*
 * Copyright 2017 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.client

import tech.aroma.client.exceptions.AromaException
import tech.aroma.thrift.application.service.ApplicationService
import tech.aroma.thrift.application.service.ApplicationServiceConstants
import tech.aroma.thrift.authentication.ApplicationToken
import tech.aroma.thrift.endpoint.Endpoint
import tech.aroma.thrift.endpoint.TcpEndpoint
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty
import tech.sirwellington.alchemy.annotations.arguments.Optional
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.validPort
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Send Messages from your Application using this interface.
 *
 *
 * Begin a new message with [Aroma.begin] and finish with [Aroma.Request.send];

 * @author SirWellington
 */
@ThreadSafe
@BuilderPattern(role = PRODUCT)
@FluidAPIDesign
interface Aroma
{

    /**
     * Set your very own hostname for messages sent to Aroma.
     * This defaults to the [name of the localhost][java.net.InetAddress.getLocalHost].
     */
    var hostname: String

    /**
     * SEt your very own device name for messages sent to Aroma.
     * This defaults to the [name of the localhost][java.net.InetAddress.getLocalHost],
     * just like [hostname].
     */
    var deviceName: String

    var bodyPrefix: String

    var bodySuffix: String

    fun clearSuffix()
    {
        bodySuffix = ""
    }

    fun clearPrefix()
    {
        bodyPrefix = ""
    }

    /**
     * Begin a new Aroma Message.

     * @return
     */
    fun begin(): Request


    interface Request
    {
        /**
         * Set the Body of the Message.

         * @param message
         * *
         * @param args
         * *
         * @return
         */
        fun withBody(@Required message: String, @Optional vararg args: Any): Request

        /**
         * Set the Title of the Message.

         * @param title
         * *
         * @return
         */
        fun titled(@Required title: String): Request

        /**
         * Set the Priority or Urgency of the Message.

         * @param priority
         * *
         * @return
         * *
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun withPriority(@Required priority: Priority): Request

        /**
         * Sends the Message to Aroma. This method must be called, or else the message won't be sent.

         * @throws IllegalArgumentException
         * *
         * @throws AromaException
         */
        @Throws(IllegalArgumentException::class, AromaException::class)
        fun send()
    }

    /**
     * Convenience method to send a message with [Low Priority][Priority.LOW].

     * @param title Title of the message
     */
    fun sendLowPriorityMessage(@NonEmpty title: String)
    {
        sendLowPriorityMessage(title, "")
    }

    /**
     * Convenience method to send a message with [Low Priority][Priority.LOW].

     * @param title Title of the message
     * *
     * @param body  Body of the message
     * *
     * @param args  Any arguments for the body.
     */
    fun sendLowPriorityMessage(@NonEmpty title: String, @NonEmpty body: String, vararg args: Any)
    {
        sendMessage(Priority.LOW, title, body, *args)
    }

    /**
     * Convenience method to send a message with [Medium Priority][Priority.MEDIUM].

     * @param title Title of the message
     */
    fun sendMediumPriorityMessage(@NonEmpty title: String)
    {
        sendMediumPriorityMessage(title, "")
    }

    /**
     * Convenience method to send a message with [Medium Priority][Priority.MEDIUM].

     * @param title Title of the message
     * *
     * @param body  Body of the message
     * *
     * @param args  Any arguments for the body
     */
    fun sendMediumPriorityMessage(@NonEmpty title: String, @NonEmpty body: String, vararg args: Any)
    {
        sendMessage(Priority.MEDIUM, title, body, *args)
    }

    /**
     * Convenience method to send a message with [High Priority][Priority.HIGH].

     * @param title Title of the message
     */
    fun sendHighPriorityMessage(@NonEmpty title: String)
    {
        sendHighPriorityMessage(title, "")
    }

    /**
     * Convenience method to send a message with [High Priority][Priority.HIGH].

     * @param title Title of the message
     * *
     * @param body  Body of the message
     * *
     * @param args  Any arguments for the body
     */
    fun sendHighPriorityMessage(@NonEmpty title: String, @NonEmpty body: String, vararg args: Any)
    {
        sendMessage(Priority.HIGH, title, body, *args)
    }

    /**
     * Convenience method to quickly send a method in one function call.

     * @param priority The priority of the message
     * *
     * @param title    The message title
     * *
     * @param body     The body of the message
     * *
     * @param args     Any string arguments passed
     */
    fun sendMessage(@Required priority: Priority, @NonEmpty title: String, @NonEmpty body: String, vararg args: Any)
    {
        checkThat(priority).isA(notNull())

        checkThat(title)
                .usingMessage("title cannot be empty")
                .isA(nonEmptyString())

        var request = begin().withPriority(priority)
                .titled(title)

        if (body.isNotEmpty())
        {
            request = request.withBody(body, *args)
        }

        request.send()
    }

    /**
     * Use a Builder to create a more fine-tuned [Aroma Client][Aroma].
     */
    @BuilderPattern(role = BUILDER)
    class Builder internal constructor()
    {

        private var hostname = ApplicationServiceConstants.PRODUCTION_ENDPOINT.getHostname()
        private var port = ApplicationServiceConstants.PRODUCTION_ENDPOINT.getPort()
        private var applicationToken = ""
        private var async: Executor? = null

        /**
         * Set the Token ID created from the Aroma App.

         * @param applicationToken
         * *
         * @return
         * *
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun withApplicationToken(@Required applicationToken: String): Builder
        {
            checkThat(applicationToken)
                    .isA(nonEmptyString())

            this.applicationToken = applicationToken

            return this
        }

        /**
         * If you are using your own Aroma Server, you can set a custom endpoint for your Client to communicate with
         * here.

         * @param hostname
         * *
         * @param port
         * *
         * @return
         * *
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun withEndpoint(@NonEmpty hostname: String, port: Int): Builder
        {
            checkThat(hostname)
                    .usingMessage("hostname cannot be empty")
                    .isA(nonEmptyString())

            checkThat(port)
                    .isA(validPort())

            this.hostname = hostname
            this.port = port

            return this
        }

        /**
         * Set an [Executor] to be used for making asynchronous requests. Note that if one isn't specified, a
         * [Single-Threaded Executor][Executors.newSingleThreadExecutor] is used instead.

         * @param executor
         * *
         * @return
         * *
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun withAsyncExecutor(@Required executor: Executor): Builder
        {
            checkThat(executor).isA(notNull())

            this.async = executor

            return this
        }

        /**
         * @deprecated in favor of [withAsyncExecutor].
         */
        @Deprecated("Use 'withAsyncExecutor' instead", replaceWith = ReplaceWith("withAsyncExecutor"))
        @Throws(IllegalArgumentException::class)
        fun withAsyncExecutorService(@Required executor: ExecutorService): Builder
        {
            return this.withAsyncExecutor(executor)
        }

        /**
         * Creates the Aroma Client.

         * @return
         * *
         * @throws IllegalStateException
         */
        @Throws(IllegalStateException::class)
        fun build(): Aroma
        {
            checkThat(hostname)
                    .throwing(IllegalStateException::class.java)
                    .usingMessage("missing hostname")
                    .isA(nonEmptyString())

            checkThat(applicationToken)
                    .throwing(IllegalStateException::class.java)
                    .usingMessage("missing Application Token")
                    .isA(nonEmptyString())

            checkThat(port)
                    .throwing(IllegalStateException::class.java)
                    .isA(validPort())

            val executor = async ?: Executors.newSingleThreadExecutor()

            val endpoint = createEndpoint()

            val token = ApplicationToken().setTokenId(applicationToken)

            val thriftClientProvider = ThriftClientProvider { endpoint }
            val clientProvider =  Provider<ApplicationService.Iface> {
                thriftClientProvider.get()
            }
            val aroma = AromaClient(clientProvider, executor, token)
            return aroma

        }

        private fun createEndpoint(): Endpoint
        {
            val tcpEndpoint = TcpEndpoint(hostname, port)

            val endpoint = Endpoint()
            endpoint.tcp = tcpEndpoint
            return endpoint
        }

        companion object
        {
            /**
             * Create a new Builder.

             * @return
             */
            @JvmStatic
            fun create(): Builder
            {
                return Builder()
            }
        }

    }

    companion object Factory
    {

        /**
         * Creates a default Aroma Client using the specified application token.

         * @param applicationToken The unique Application Token created from the Aroma App.
         * *
         * @return
         * *
         * @see [http://aroma.redroma.tech/how-to](http://aroma.redroma.tech/how-to)

         * @see [http://redroma.github.io/aroma-java-client/](http://redroma.github.io/aroma-java-client/)
         */
        fun create(@NonEmpty applicationToken: String): Aroma
        {
            checkThat(applicationToken)
                    .usingMessage("Application Token cannot be empty")
                    .isA(nonEmptyString())

            return newBuilder()
                    .withAsyncExecutor(Executors.newSingleThreadExecutor())
                    .withApplicationToken(applicationToken)
                    .build()
        }

        /**
         * Creates an Aroma Client that does absolutely nothing with the messages sent.
         *
         *
         * This is useful for testing purposes when you don't want messages sent over the wire.

         * @return
         */
        fun createNoOpInstance(): Aroma
        {
            return AromaDoNothingClient.INSTANCE
        }

        /**
         * Use a Builder to create a more fine-tuned [Aroma Client][Aroma].

         * @return
         */
        fun newBuilder(): Builder
        {
            return Builder()
        }
    }

}

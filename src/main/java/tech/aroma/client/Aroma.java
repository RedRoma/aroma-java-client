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

package tech.aroma.client;

import tech.aroma.client.exceptions.AromaException;
import tech.aroma.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.endpoint.Endpoint;
import tech.aroma.thrift.endpoint.TcpEndpoint;
import tech.sirwellington.alchemy.annotations.arguments.*;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.arguments.Checks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validPort;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * Send Messages from your Application using this interface. 
 * 
 * Begin a new message with {@link Aroma#begin() } and finish with {@link Aroma.Request#send() };
 * 
 * @author SirWellington
 */
@ThreadSafe
@BuilderPattern(role = PRODUCT)
@FluidAPIDesign
public interface Aroma
{
    
    /**
     * Begin a new Aroma Message.
     * 
     * @return 
     */
    Request begin();
    
    
    interface Request
    {
        /**
         * Set the Body of the Message.
         * 
         * @param message
         * @param args
         * @return 
         */
        Request withBody(@Required String message, @Optional Object...args);
        
        /**
         * Set the Title of the Message.
         * 
         * @param title
         * @return 
         */
        Request titled(@Required String title);
        
        /**
         * Set the Priority or Urgency of the Message.
         * 
         * @param priority
         * @return
         * @throws IllegalArgumentException 
         */
        Request withPriority(@Required Priority priority) throws IllegalArgumentException;
        
        /**
         * Sends the Message to Aroma. This method must be called, or else the message won't be sent.
         * 
         * @throws IllegalArgumentException
         * @throws AromaException 
         */
        void send() throws IllegalArgumentException, AromaException;
    }

    /**
     * Convenience method to send a message with {@linkplain Priority#LOW Low Priority}.
     *
     * @param title Title of the message
     */
    void sendLowPriorityMessage(@NonEmpty String title);

    /**
     * Convenience method to send a message with {@linkplain Priority#LOW Low Priority}.
     *
     * @param title Title of the message
     * @param body Body of the message
     * @param args Any arguments for the body.
     */
    default void sendLowPriorityMessage(@NonEmpty String title, @NonEmpty String body, Object... args)
    {
        sendMessage(Priority.LOW, title, body, args);
    }

    /**
     * Convenience method to send a message with {@linkplain Priority#MEDIUM Medium Priority}.
     *
     * @param title Title of the message
     */
    void sendMediumPriorityMessage(@NonEmpty String title);

    /**
     * Convenience method to send a message with {@linkplain Priority#MEDIUM Medium Priority}.
     *
     * @param title Title of the message
     * @param body Body of the message
     * @param args Any arguments for the body
     */
    void sendMediumPriorityMessage(@NonEmpty String title, @NonEmpty String body, Object... args);

    /**
     * Convenience method to send a message with {@linkplain Priority#HIGH High Priority}.
     *
     * @param title Title of the message
     */
    void sendHighPriorityMessage(@NonEmpty String title);

    /**
     * Convenience method to send a message with {@linkplain Priority#HIGH High Priority}.
     *
     * @param title Title of the message
     * @param body Body of the message
     * @param args Any arguments for the body
     */
    void sendHighPriorityMessage(@NonEmpty String title, @NonEmpty String body, Object... args);

    /**
     * Convenience method to quickly send a method in one function call.
     *
     * @param priority The priority of the message
     * @param title The message title
     * @param body The body of the message
     * @param args Any string arguments passed
     */
    void sendMessage(@Required Priority priority, @NonEmpty String title, @NonEmpty String body, Object... args);

    /**
     * Creates a default Aroma Client using the specified application token.
     * 
     * @param applicationToken The unique Application Token created from the Aroma App.
     * @return 
     * @see <a href="http://aroma.redroma.tech/how-to">http://aroma.redroma.tech/how-to</a>
     * @see <a href="http://redroma.github.io/aroma-java-client/">http://redroma.github.io/aroma-java-client/</a>
     */
    static Aroma create(@NonEmpty String applicationToken)
    {
        checkThat(applicationToken)
            .usingMessage("Application Token cannot be empty")
            .is(nonEmptyString());
        
        return newBuilder()
            .withAsyncExecutorService(Executors.newSingleThreadExecutor())
            .withApplicationToken(applicationToken)
            .build();
    }

    /**
     * Creates an Aroma Client that does absolutely nothing with the messages sent.
     *
     * This is useful for testing purposes when you don't want messages sent over the wire.
     *
     * @return
     */
    static Aroma createNoOpInstance()
    {
        return AromaDoNothingClient.INSTANCE;
    }
    
    /**
     * Use a Builder to create a more fine-tuned {@linkplain Aroma Aroma Client}.
     * @return 
     */
    static Builder newBuilder()
    {
        return new Builder();
    }
    
    /**
     * Use a Builder to create a more fine-tuned {@linkplain Aroma Aroma Client}.
     */
    @BuilderPattern(role = BUILDER)
    static final class Builder 
    {
        /**
         * Create a new Builder.
         * @return 
         */
        static Builder create()
        {
            return new Builder();
        }

        private String hostname = ApplicationServiceConstants.PRODUCTION_ENDPOINT.getHostname();
        private int port = ApplicationServiceConstants.PRODUCTION_ENDPOINT.getPort();
        private String applicationToken = "";
        private ExecutorService async;
        
        Builder() 
        {
            
        }
        
        /**
         * Set the Token ID created from the Aroma App.
         * 
         * @param applicationToken
         * @return
         * 
         * @throws IllegalArgumentException 
         */
        public Builder withApplicationToken(@Required String applicationToken) throws IllegalArgumentException
        {
            checkThat(applicationToken)
                .is(nonEmptyString());
            
            this.applicationToken = applicationToken;
            
            return this;
        }
        
        /**
         * If you are using your own Aroma Server, you can set a custom endpoint for your Client to communicate with
         * here.
         * 
         * @param hostname
         * @param port
         * 
         * @return
         * @throws IllegalArgumentException 
         */
        public Builder withEndpoint(@NonEmpty String hostname, int port) throws IllegalArgumentException
        {
            checkThat(hostname)
                .usingMessage("hostname cannot be empty")
                .is(nonEmptyString());
            
            checkThat(port)
                .is(validPort());
                
            this.hostname = hostname;
            this.port = port;
            
            return this;
        }

        /**
         * Set an {@link ExecutorService} to be used for making asynchronous requests. Note that if one isn't specified, a
         * {@linkplain Executors#newSingleThreadExecutor() Single-Threaded Executor} is used instead.
         *
         * @param executor
         * @return
         * @throws IllegalArgumentException 
         */
        public Builder withAsyncExecutorService(@Required ExecutorService executor) throws IllegalArgumentException
        {
            checkThat(executor)
                .is(notNull());
            
            this.async = executor;
            
            return this;
        }
        
        /**
         * Creates the Aroma Client.
         * 
         * @return
         * @throws IllegalStateException 
         */
        public Aroma build() throws IllegalStateException
        {
            checkThat(hostname)
                .throwing(IllegalStateException.class)
                .usingMessage("missing hostname")
                .is(nonEmptyString());
            
            checkThat(applicationToken)
                .throwing(IllegalStateException.class)
                .usingMessage("missing Application Token")
                .is(nonEmptyString());
            
            checkThat(port)
                .throwing(IllegalStateException.class)
                .is(validPort());
            
            if (async == null)
            {
                async = Executors.newSingleThreadExecutor();
            }
            
            Endpoint endpoint = createEndpoint();
            
            ApplicationToken token = new ApplicationToken().setTokenId(applicationToken);
            
            ThriftClientProvider clientProvider = new ThriftClientProvider(() -> endpoint);
            AromaClient aroma = new AromaClient(() -> clientProvider.get(), async, token);
            return aroma;
            
        }

        private Endpoint createEndpoint()
        {
            TcpEndpoint tcpEndpoint = new TcpEndpoint(hostname, port);

            Endpoint endpoint = new Endpoint();
            endpoint.setTcp(tcpEndpoint);
            return endpoint;
        }
        
    }
    
}

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

import org.slf4j.LoggerFactory
import org.slf4j.helpers.MessageFormatter
import tech.aroma.thrift.application.service.ApplicationServiceConstants
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.arguments.*
import tech.sirwellington.alchemy.annotations.concurrency.Immutable
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import java.io.*

/**
 * @author SirWellington
 */
@Immutable
@Internal
internal class RequestImpl : Aroma.Request
{

    internal val aromaClient: AromaClient
    internal val title: String
    internal val text: String
    internal val priority: Priority

    constructor(@Required aromaClient: AromaClient,
                @Required @NonEmpty title: String,
                @Required @NonEmpty text: String,
                @Required priority: Priority)
    {
        checkThat(aromaClient, title, text, priority)
                .are(notNull())

        this.aromaClient = aromaClient
        this.title = title
        this.text = text
        this.priority = priority
    }

    override fun titled(title: String): Aroma.Request
    {
        checkThat(title)
                .usingMessage("title cannot be empty")
                .isA(nonEmptyString())
                .usingMessage("title too short")
                .isA(stringWithLengthGreaterThanOrEqualTo(3))
                .usingMessage("title too long")
                .isA(stringWithLengthLessThan(ApplicationServiceConstants.MAX_TITLE_LENGTH))

        return RequestImpl(aromaClient, title, text, priority)
    }

    override fun withBody(message: String, @Optional vararg args: Any): Aroma.Request
    {
        checkThat(message)
                .usingMessage("message cannot be null")
                .isA(notNull())

        val combinedMessage = combineStringAndArgs(message, *args)
        return RequestImpl(aromaClient, title, combinedMessage, priority)
    }

    private fun combineStringAndArgs(message: String, vararg args: Any): String
    {
        if (args.isEmpty())
        {
            return message
        }

        val arrayFormat = MessageFormatter.arrayFormat(message, args)
        val formattedMessage = arrayFormat.message

        val ex = arrayFormat.throwable

        if (ex == null)
        {
            return formattedMessage
        }
        else
        {
            return String.format("%s\n%s", formattedMessage, printThrowable(ex))
        }
    }

    private fun printThrowable(ex: Throwable): String
    {

        try
        {
            StringWriter().use { stringWriter ->
                PrintWriter(stringWriter).use { printWriter ->
                    ex.printStackTrace(printWriter)
                    return stringWriter.toString()
                }
            }
        }
        catch (ioex: IOException)
        {
            LOG.info("Failed to close String and Print Writers", ioex)
            return ex.message ?: ""
        }

    }

    @Throws(IllegalArgumentException::class)
    override fun withPriority(@Required priority: Priority): Aroma.Request
    {
        checkThat(priority)
                .usingMessage("priority cannot be null")
                .isA(notNull())

        return RequestImpl(aromaClient, title, text, priority)
    }

    @Throws(IllegalArgumentException::class)
    override fun send()
    {
        aromaClient.sendMessage(this)
    }

    override fun toString(): String
    {
        return "RequestImpl{aromaClient=$aromaClient, priority=$priority, title=$title, text=$text}"
    }

    companion object
    {
        private val LOG = LoggerFactory.getLogger(RequestImpl::class.java)
    }

}

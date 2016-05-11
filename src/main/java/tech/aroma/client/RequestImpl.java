/*
 * Copyright 2016 RedRoma, Inc.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.arguments.assertions.Assertions;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthLessThan;

/**
 *
 * @author SirWellington
 */
@Immutable
@Internal
final class RequestImpl implements Aroma.Request
{
    private static final Logger LOG = LoggerFactory.getLogger(RequestImpl.class);

    private final AromaClient aromaClient;

    private final Urgency urgency;
    private final String title;
    private final String text;

    RequestImpl(@Required AromaClient aromaClient,
                @Required String title,
                @Required String text,
                @Required Urgency urgency)
    {
        checkThat(aromaClient, title, text, urgency)
            .are(notNull());

        this.aromaClient = aromaClient;
        this.title = title;
        this.text = text;
        this.urgency = urgency;
    }

    @Override
    public Aroma.Request titled(String title)
    {
        checkThat(title)
            .usingMessage("title cannot be empty")
            .is(nonEmptyString())
            .usingMessage("title too short")
            .is(stringWithLengthGreaterThanOrEqualTo(3))
            .usingMessage("title too long")
            .is(stringWithLengthLessThan(40));

        return new RequestImpl(aromaClient, title, text, urgency);
    }

    @Override
    public Aroma.Request text(String message, @Optional Object... args)
    {
        checkThat(message)
            .usingMessage("message cannot be null")
            .is(notNull());

        String combinedMessage = combineStringAndArgs(message, args);
        return new RequestImpl(aromaClient, title, combinedMessage, urgency);
    }

    private String combineStringAndArgs(String message, Object... args)
    {
        if (args == null || args.length == 0)
        {
            return message;
        }

        FormattingTuple arrayFormat = MessageFormatter.arrayFormat(message, args);
        String formattedMessage = arrayFormat.getMessage();

        Throwable ex = arrayFormat.getThrowable();

        if(ex == null)
        {
            return formattedMessage;
        }
        else
        {
            return String.format("%s\n%s", formattedMessage, printThrowable(ex));
        }
    }

    private String printThrowable(Throwable ex)
    {

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter);)
        {
            ex.printStackTrace(printWriter);
            return stringWriter.toString();
        }
        catch(IOException ioex)
        {
            LOG.info("Failed to close String and Print Writers", ioex);
            return ex.getMessage();
        }
    }

    @Override
    public Aroma.Request withUrgency(@Required Urgency level) throws IllegalArgumentException
    {
        checkThat(level)
            .usingMessage("urgency cannot be null")
            .is(Assertions.notNull());

        return new RequestImpl(aromaClient, title, text, level);
    }

    @Override
    public void send() throws IllegalArgumentException
    {
        aromaClient.sendMessage(this);
    }

    @Internal
    String getText()
    {
        return this.text;
    }

    @Internal
    String getTitle()
    {
        return this.title;
    }

    @Internal
    Urgency getUrgency()
    {
        return this.urgency;
    }

    @Override
    public String toString()
    {
        return "RequestImpl{" + "aromaClient=" + aromaClient + ", urgency=" + urgency + ", title=" + title + ", text=" + text + '}';
    }

}

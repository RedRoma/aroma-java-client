/*
 * Copyright 2016 Aroma Tech.
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

package tech.aroma.banana.client;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.arguments.assertions.Assertions;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
final class RequestImpl implements Banana.Request
{

    private final BananaClient bananaClient;

    private Urgency urgency = Urgency.LOW;
    private String message = "";

    RequestImpl(@Required BananaClient bananaClient)
    {
        Arguments.checkThat(bananaClient).is(Assertions.notNull());
        
        this.bananaClient = bananaClient;
    }

    @Override
    public Banana.Request message(String message, @Optional Object... args)
    {
        checkThat(message)
            .usingMessage("message cannot be null")
            .is(notNull());
        
        String combinedMessage = combineStringAndArgs(message, args);
        this.message = combinedMessage;
        return this;
    }

    private String combineStringAndArgs(String message, Object... args)
    {
        if (args == null || args.length == 0)
        {
            return message;
        }
        
        FormattingTuple arrayFormat = MessageFormatter.arrayFormat(message, args);
        return arrayFormat.getMessage();
    }

    @Override
    public Banana.Request withUrgency(@Required Urgency level) throws IllegalArgumentException
    {
        Arguments.checkThat(level).usingMessage("urgency cannot be null").is(Assertions.notNull());
        this.urgency = level;
        return this;
    }

    @Override
    public void send() throws IllegalArgumentException
    {
        bananaClient.sendMessage(this);
    }

    @Internal
    String getMessage()
    {
        return this.message;
    }

    @Internal
    Urgency getUrgency()
    {
        return this.urgency;
    }

}
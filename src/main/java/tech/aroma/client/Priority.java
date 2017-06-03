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

import tech.aroma.thrift.Urgency;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/*
 * This class exists to give some Schema Isolation to Clients from any changes that happen in the Thrift Specification.
 */

/**
 * Describes how important a message is.
 *
 * @author SirWellington
 */
public enum Priority
{

    /**
     * LOW Messages are like an FYI; they are not important but you may want to know
     * about it. For example, a new user sign-up for your service, or a post was flagged by a user.
     */
    LOW(Urgency.LOW),

    /**
     * MEDIUM Messages are considered Important.
     */
    MEDIUM(Urgency.MEDIUM),

    /**
     * HIGH messages typically indicate Show-Stopping events, such as a Database going down,
     * or a network connection issue. It could also be a great thing, such as a customer spending a
     * significant amount of money in your App.
     */
    HIGH(Urgency.HIGH);

    private final Urgency thriftUrgency;

    private Priority(@Required Urgency thriftUrgency)
    {
        checkThat(thriftUrgency).is(notNull());

        this.thriftUrgency = thriftUrgency;
    }

    /**
     * Map to the underlying Thrift version.
     *
     * @return
     */
    Urgency toThrift()
    {
        return thriftUrgency;
    }
}

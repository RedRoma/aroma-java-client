/*
 * Copyright 2018 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AlchemyTestRunner.class)
public class AromaDoNothingClientTest
{

    @GenerateString
    private String title;

    @GenerateString
    private String body;

    @GenerateEnum
    private Priority priority;

    private AromaDoNothingClient instance;


    @Before
    public void setup()
    {
        instance = AromaDoNothingClient.Companion.getINSTANCE();
    }

    @Test
    public void begin() throws Exception
    {
        Aroma.Request request = instance.begin();
        assertThat(request, notNullValue());
    }

    @Test
    public void testSendMessage() throws Exception
    {
        instance.begin()
                .titled(title)
                .withBody(body, body)
                .withPriority(priority);
    }

}
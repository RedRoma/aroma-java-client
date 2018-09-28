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
import tech.aroma.thrift.Urgency;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class PriorityTest
{

    @Before
    public void setUp()
    {
    }

    @Test
    public void testToThrift()
    {
        assertThat(Priority.LOW.toThrift(),
                   is(Urgency.LOW));

        assertThat(Priority.MEDIUM.toThrift(),
                   is(Urgency.MEDIUM));

        assertThat(Priority.HIGH.toThrift(),
                   is(Urgency.HIGH));
    }

}

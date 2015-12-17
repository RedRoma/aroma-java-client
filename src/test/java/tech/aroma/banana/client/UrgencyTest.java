/*
 * Copyright 2015 Aroma Tech.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class UrgencyTest 
{

    @Before
    public void setUp()
    {
    }

    @Test
    public void testToThrift()
    {
        assertThat(Urgency.INFORMATIONAL.toThrift(),
                   is(tech.aroma.banana.thrift.Urgency.INFORMATIONAL));
        
        assertThat(Urgency.WARNING.toThrift(),
                   is(tech.aroma.banana.thrift.Urgency.WARNING));
        
        assertThat(Urgency.CRITICAL.toThrift(),
                   is(tech.aroma.banana.thrift.Urgency.CRITICAL));
    }

}
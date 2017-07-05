package tech.aroma.client

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

import com.google.common.util.concurrent.MoreExecutors
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.aroma.client.Priority.MEDIUM
import tech.aroma.thrift.application.service.ApplicationServiceConstants
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import tech.sirwellington.alchemy.test.junit.runners.Repeat

@RunWith(AlchemyTestRunner::class)
class AromaIT
{
    private val hostname = ApplicationServiceConstants.PRODUCTION_ENDPOINT.hostname
    private val port = 80

    @GenerateString(UUID)
    private lateinit var appToken: String

    @GenerateString(ALPHABETIC)
    private lateinit var body: String

    private lateinit var aroma: Aroma

    @Before
    fun setUp()
    {
        aroma = Aroma.newBuilder()
                .withEndpoint(hostname, port)
                .withApplicationToken(appToken)
                .withAsyncExecutorService(MoreExecutors.newDirectExecutorService())
                .build()
    }

    @Repeat(25)
    @Test
    fun testSendMessage()
    {
        aroma.sendLowPriorityMessage("Unit Test", body)

        aroma.begin().titled("Unit Test")
                .withBody(body)
                .withPriority(MEDIUM)
                .send()
    }

}
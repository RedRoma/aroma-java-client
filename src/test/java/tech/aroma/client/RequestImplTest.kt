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

package tech.aroma.client

import com.google.common.util.concurrent.MoreExecutors
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import tech.aroma.thrift.Urgency
import tech.aroma.thrift.application.service.ApplicationService
import tech.aroma.thrift.application.service.ApplicationService.Iface
import tech.aroma.thrift.application.service.ApplicationServiceConstants
import tech.aroma.thrift.application.service.SendMessageRequest
import tech.aroma.thrift.authentication.ApplicationToken
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.epochNowWithinDelta
import tech.sirwellington.alchemy.generator.EnumGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import kotlin.test.assertTrue

/**
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner::class)
class RequestImplTest
{

    @Mock
    private lateinit var applicationService: ApplicationService.Iface

    @GeneratePojo
    private lateinit var token: ApplicationToken

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<SendMessageRequest>

    private lateinit var aromaClient: AromaClient

    private lateinit var instance: RequestImpl

    @GenerateString
    private lateinit var body: String

    @GenerateString
    private lateinit var title: String

    @GenerateEnum
    private lateinit var priority: Priority

    @GenerateString(ALPHABETIC)
    private lateinit var exceptionMessage: String

    private lateinit var ex: Exception

    @Before
    fun setUp()
    {
        ex = RuntimeException(exceptionMessage)

        val executor = MoreExecutors.newDirectExecutorService()
        val provider = createProviderFor(applicationService)

        aromaClient = AromaClient(provider, executor, token)

        instance = RequestImpl(aromaClient, title, body, priority)
    }

    @Test
    fun testMessage()
    {
        val newMessage = one(alphabeticStrings(100))

        val result = instance.withBody(newMessage)

        assertThat(result, isA<RequestImpl>())
        assertThat(result, !sameInstance<Any>(instance))

        val newRequest = result as RequestImpl
        assertThat(newRequest.priority, equalTo(instance.priority))
        assertThat(newRequest.text, equalTo(newMessage))
    }

    @Test
    fun testMessageFormatting()
    {
        val first = one(alphabeticStrings(5))
        val second = one(alphabeticStrings(5))
        val third = one(alphabeticStrings(5))

        val formattedMessage = "First {} Second {} Third {}"
        val expected = "First $first Second $second Third $third"

        val result = instance.withBody(formattedMessage, first, second, third)
        assertTrue { result is RequestImpl }

        var request = result as RequestImpl
        assertThat(request.text, equalTo(expected))

        request = instance.withBody(formattedMessage, first, second, third, ex) as RequestImpl
        assertThat(request.text, containsSubstring(first))
        assertThat(request.text, containsSubstring(second))
        assertThat(request.text, containsSubstring(third))
        assertThat(request.text, containsSubstring(exceptionMessage))
        assertThat(request.text, containsSubstring(ex.javaClass.name))

    }

    @Test
    fun testWithPriority()
    {
        val newPriority = EnumGenerators.enumValueOf<Priority>().get()
        val result = instance.withPriority(newPriority)
        assertThat(result, isA<RequestImpl>())
        assertThat(result, !(sameInstance<Any>(instance)))

        val newRequest = result as RequestImpl
        assertThat(newRequest.priority, equalTo(newPriority))
        assertThat(newRequest.text, equalTo(instance.text))
    }

    @Test
    @Throws(Exception::class)
    fun testSend()
    {
        instance.send()

        verify<Iface>(applicationService).sendMessage(requestCaptor.capture())

        val request = requestCaptor.value
        assertThat(request, !absent<Any>())
        assertThat(request.body, equalTo(body))
        assertThat(request.title, equalTo(title))
        assertThat<Urgency>(request.urgency, equalTo(priority.toThrift()))
        assertThat(request.applicationToken, equalTo(token))

        checkThat(request.timeOfMessage)
                .isA(epochNowWithinDelta(1000L))
    }

    @Test
    fun testGetMessage()
    {
        val result = instance.text
        assertThat(result, equalTo(body))
    }

    @Test
    fun testPriority()
    {
        val result = instance.priority
        assertThat(result, equalTo(priority))
    }

    @Test
    fun testGetTitle()
    {
        val result = instance.title
        assertThat(result, equalTo(title))
    }

    @Test
    fun testTitled()
    {
        val newTitle = one(alphabeticStrings(10))

        val result = instance.titled(newTitle)
        assertThat(result, isA<RequestImpl>())
        assertThat(result, !(sameInstance<Any>(instance)))

        val newRequest = result as RequestImpl
        assertThat(newRequest.priority, equalTo(instance.priority))
        assertThat(newRequest.text, equalTo(body))
        assertThat(newRequest.title, equalTo(newTitle))
    }

    @Test
    fun testWithLongTitle()
    {
        val length = one(integers(ApplicationServiceConstants.MAX_TITLE_LENGTH + 1,
                                  ApplicationServiceConstants.MAX_TITLE_LENGTH * 2))

        val longTitle = one(alphabeticStrings(length))
        assertThrows { instance.titled(longTitle) }
    }

    @Test
    fun testWithShortTitle()
    {
        val length = one(integers(1, 2))

        val shortTitle = one(alphabeticStrings(length))
        assertThrows { instance.titled(shortTitle) }
    }

    @DontRepeat
    @Test
    fun testWithEmptyTitle()
    {
        assertThrows { instance.titled("") }
    }

    private fun createProviderFor(applicationService: ApplicationService.Iface): Provider<Iface>
    {
        return Provider { applicationService }
    }
}

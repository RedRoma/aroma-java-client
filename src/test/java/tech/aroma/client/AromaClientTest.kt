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

import com.google.common.util.concurrent.MoreExecutors
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.apache.thrift.protocol.TProtocol
import org.apache.thrift.transport.TTransport
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import tech.aroma.thrift.Urgency
import tech.aroma.thrift.application.service.ApplicationService
import tech.aroma.thrift.application.service.SendMessageRequest
import tech.aroma.thrift.authentication.ApplicationToken
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.nowWithinDelta
import tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.time.Instant

/**
 * @author SirWellington
 */
@Repeat
@RunWith(AlchemyTestRunner::class)
class AromaClientTest
{

    @Mock
    private lateinit var protocol: TProtocol

    @Mock
    private lateinit var transport: TTransport

    @Mock
    private lateinit var applicationService: ApplicationService.Client

    @GeneratePojo
    private lateinit var token: ApplicationToken

    private val executor = MoreExecutors.newDirectExecutorService()

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<SendMessageRequest>

    private lateinit var request: RequestImpl

    private lateinit var instance: AromaClient

    @GenerateString
    private lateinit var body: String

    @GenerateString
    private lateinit var title: String

    private lateinit var priority: Priority

    @Before
    fun setUp()
    {
        priority = enumValueOf(Priority::class.java).get()

        val serviceProvider = Provider<ApplicationService.Iface>{ applicationService }

        instance = AromaClient(serviceProvider, executor, token)

        request = RequestImpl(instance, title, body, priority)

        setupThriftTransports()
    }

    private fun setupThriftTransports()
    {

        whenever(applicationService.inputProtocol)
                .thenReturn(protocol)
        whenever(applicationService.outputProtocol)
                .thenReturn(protocol)

        whenever(protocol.transport)
                .thenReturn(transport)
    }

    @Test
    fun testBegin()
    {
        val result = instance.begin()
        assertThat(result, notNullValue())
        assertThat(result, instanceOf<Any>(RequestImpl::class.java))
    }

    @TimeSensitive
    @Test
    @Throws(Exception::class)
    fun testSendMessage()
    {
        instance.sendMessage(request)

        verify(applicationService).sendMessage(requestCaptor!!.capture())

        val requestMade = requestCaptor.value
        assertThat(requestMade, notNullValue())
        assertThat(requestMade.body, `is`<String>(body))
        assertThat(requestMade.title, `is`<String>(title))
        assertThat<Urgency>(requestMade.urgency, `is`<Urgency>(priority!!.toThrift()))
        assertThat(requestMade.applicationToken, `is`<ApplicationToken>(token))

        val timeOfMessage = Instant.ofEpochMilli(requestMade.timeOfMessage)
        checkThat(timeOfMessage)
                .`is`(nowWithinDelta(1000L))

        verify<TTransport>(transport, atLeastOnce()).close()
    }

    @Test
    @Throws(Exception::class)
    fun testSendMessageWhenOperationFails()
    {
        whenever(applicationService.sendMessage(any()))
                .thenThrow(OperationFailedException())

        instance.sendMessage(request)
    }


}

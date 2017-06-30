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
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.capture
import com.nhaarman.mockito_kotlin.whenever
import org.apache.thrift.protocol.TProtocol
import org.apache.thrift.transport.TTransport
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import tech.aroma.thrift.application.service.ApplicationService
import tech.aroma.thrift.application.service.SendMessageRequest
import tech.aroma.thrift.authentication.ApplicationToken
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import tech.sirwellington.alchemy.generator.EnumGenerators.Companion.enumValueOf
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @GenerateString
    private lateinit var hostname: String

    @GenerateString
    private lateinit var deviceName: String

    @Before
    fun setUp()
    {
        priority = one(enumValueOf<Priority>())

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
        assertThat(result, notNull)
        assertTrue { result is RequestImpl }
    }

    @TimeSensitive
    @Test
    @Throws(Exception::class)
    fun testSendMessage()
    {
        instance.sendMessage(request)

        verify(applicationService).sendMessage(requestCaptor.capture())

        val requestMade = requestCaptor.value
        assertFalse { requestMade == null }
        assertThat(requestMade.body, equalTo(body))
        assertThat(requestMade.title, equalTo(title))
        assertThat(requestMade.urgency, equalTo(priority.toThrift()))
        assertThat(requestMade.applicationToken, equalTo(token))

        checkThat(requestMade.timeOfMessage)
                .isA(epochNowWithinDelta(1000))

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

    @Test
    fun testHostname()
    {
        instance.hostname = this.hostname

        assertThat(instance.hostname, equalTo(this.hostname))
    }

    @Test
    fun testHostnameIsIncludedInMessage()
    {
        instance.hostname = this.hostname

        instance.sendMessage(request)

        verify(applicationService).sendMessage(requestCaptor.capture())

        val requestMade = requestCaptor.value
        assertThat(requestMade.hostname, equalTo(hostname))
    }


    @Test
    fun testDeviceName()
    {
        instance.deviceName = this.deviceName
        assertThat(instance.deviceName, equalTo(this.deviceName))
    }

    @Test
    fun testDeviceNameIsIncludedInMessage()
    {
        instance.deviceName = this.deviceName
        instance.sendMessage(request)

        verify(applicationService).sendMessage(capture(requestCaptor))

        val requestMade = requestCaptor.value
        assertThat(requestMade, notNull)
        assertThat(requestMade.deviceName, equalTo(deviceName))
    }

}

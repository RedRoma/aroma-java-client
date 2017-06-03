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

import java.io.IOException
import java.net.ServerSocket
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.junit.*
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.aroma.client.exceptions.AromaException
import tech.aroma.thrift.application.service.ApplicationService
import tech.aroma.thrift.endpoint.*
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.test.junit.runners.*

import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE

/**
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner::class)
class ThriftClientProviderTest
{

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    @GenerateURL
    private lateinit var  url: URL

    private lateinit var  http: HttpThriftEndpoint

    private val hostname = "localhost"

    @GenerateInteger(value = RANGE, min = 7000, max = 10000)
    private var  port: Int = 0

    private lateinit var  tcp: TcpEndpoint

    private lateinit var  endpoint: Endpoint

    private lateinit var  instance: ThriftClientProvider

    private lateinit var  executor: ExecutorService
    private lateinit var  serverSocket: ServerSocket

    @Before
    @Throws(IOException::class)
    fun setUp()
    {
        setupEndpoint()
        openServerAtPort(port)

        instance = ThriftClientProvider { endpoint }
    }

    private fun setupEndpoint()
    {
        endpoint = Endpoint()
        val useHttp = one(booleans())
        if (useHttp)
        {
            http = HttpThriftEndpoint(url.toString())
            endpoint.httpThrift = http
        }
        else
        {
            tcp = TcpEndpoint(hostname, port)
            endpoint.tcp = tcp
        }
    }

    @Throws(IOException::class)
    private fun openServerAtPort(port: Int)
    {
        executor = Executors.newSingleThreadExecutor()
        try
        {
            serverSocket = ServerSocket(port)
        }
        catch (ex: IOException)
        {
            LOG.error("Could not open port at {}", port)
            throw ex
        }

        executor.submit { serverSocket.accept() }
    }

    @After
    @Throws(IOException::class)
    fun tearDown()
    {
        if (executor != null)
        {
            executor.shutdownNow()
        }

        if (serverSocket != null)
        {
            serverSocket.close()
        }
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testConstructor()
    {
        assertThrows { ThriftClientProvider(null) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { ThriftClientProvider { null } }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testGet()
    {
        val result = instance.get()
        assertThat(result, notNullValue())
    }

    @Test
    fun testWithInvalidEndpointType()
    {
        val rest = HttpRestEndpoint(url.toString())
        endpoint.httpRest = rest

        assertThrows { instance.get() }
                .isInstanceOf(AromaException::class.java)
    }

    @Test
    fun testWithBadUrl()
    {
        val badUrl = one(hexadecimalString(45))
        http = HttpThriftEndpoint(badUrl)
        endpoint.httpThrift = http

        assertThrows { instance.get() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

}

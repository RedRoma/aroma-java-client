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

import java.util.concurrent.ExecutorService

import com.google.common.util.concurrent.MoreExecutors
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.*

import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import tech.sirwellington.alchemy.generator.EnumGenerators
import tech.sirwellington.alchemy.generator.EnumGenerators.Companion
import tech.sirwellington.alchemy.generator.EnumGenerators.Companion.enumValueOf
import tech.sirwellington.alchemy.generator.NumberGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.negativeIntegers
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner::class)
class AromaTest
{

    @GenerateString
    private lateinit var hostname: String

    @GenerateInteger(value = RANGE, min = 80, max = 10000)
    private var  port: Int = 0

    @GenerateString(UUID)
    private lateinit var  applicationToken: String

    @GenerateString
    private lateinit var  title: String

    @GenerateString
    private lateinit var  body: String


    private lateinit var  executor: ExecutorService

    private lateinit var  instance: Aroma
    private lateinit var  builder: Aroma.Builder


    @Before
    fun setUp()
    {
        instance = Aroma.create(applicationToken)
        executor = MoreExecutors.newDirectExecutorService()
        builder = Aroma.newBuilder()
    }

    @Test
    fun testBegin()
    {
        val request = instance.begin()
        assertThat(request, notNullValue())
    }

    @Test
    @Throws(Exception::class)
    fun sendLowPriorityMessage()
    {

        instance.sendLowPriorityMessage(title)

        assertThrows { instance.sendLowPriorityMessage("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun sendLowPriorityMessage1()
    {
        instance.sendLowPriorityMessage(title, body)

        assertThrows { instance.sendLowPriorityMessage("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun sendMediumPriorityMessage()
    {

        instance.sendMediumPriorityMessage(title)

        assertThrows { instance.sendMediumPriorityMessage("") }
                .isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    @Throws(Exception::class)
    fun sendMediumPriorityMessage1()
    {
        instance.sendMediumPriorityMessage(title, body)

        assertThrows { instance.sendMediumPriorityMessage("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun sendHighPriorityMessage()
    {
        instance.sendHighPriorityMessage(title)

        assertThrows { instance.sendHighPriorityMessage("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun sendHighPriorityMessage1()
    {
        instance.sendHighPriorityMessage(title, body)

        assertThrows { instance.sendHighPriorityMessage("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun sendMessage()
    {

        val priority = one(enumValueOf<Priority>())

        instance.sendMessage(priority, title, body)

        assertThrows { instance.sendMessage(priority, "", body) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testCreate()
    {
        val result = Aroma.create(applicationToken)
        assertThat(result, notNullValue())
    }

    @Test
    fun testCreateWithAppToken()
    {
        val result = Aroma.create(applicationToken)
        assertThat(result, notNullValue())
        result.begin()
    }

    @Test
    fun testCreateWithBadToken()
    {
        assertThrows { Aroma.create("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @DontRepeat
    @Test
    fun testCreateNoOpInstance()
    {
        val instance = Aroma.createNoOpInstance()

        assertThat(instance, notNullValue())

        instance.sendMediumPriorityMessage(title, body)
    }

    @Test
    fun testNewBuilder()
    {
        val result = Aroma.newBuilder()
        assertThat(result, notNullValue())
    }

    @Test
    fun testBuilderWithExecutorService()
    {
        val result = builder.withAsyncExecutorService(executor)
        assertThat(result, notNullValue())
    }

    @DontRepeat
    @Test
    fun testBuilderWithExecutorServiceWithBadArgs()
    {
        val result = builder.withAsyncExecutorService(executor)
        assertThat(result, notNullValue())
    }

    @Test
    fun testBuilderWithEndpoint()
    {
        val result = builder.withEndpoint(hostname, port)
        assertThat(result, notNullValue())
    }

    @DontRepeat
    @Test
    fun testBuilderWithEndpointWithBadArgs()
    {
        val badPort = one(negativeIntegers())

        assertThrows { builder.withEndpoint(hostname, badPort) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { builder.withEndpoint(hostname, 0) }
                .isInstanceOf(IllegalArgumentException::class.java)


        assertThrows { builder.withEndpoint("", port) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testBuilderBuild()
    {
        builder.withEndpoint(hostname, port)
                .withAsyncExecutorService(executor)
                .withApplicationToken(applicationToken)

        val result = builder.build()
        assertThat(result, notNullValue())
    }

    @DontRepeat
    @Test
    fun testBuilderBuildWithBadState()
    {
        assertThrows { builder.build() }
                .isInstanceOf(IllegalStateException::class.java)

        //Missing application token
        builder = builder.withAsyncExecutorService(executor)
        assertThrows { builder.build() }
                .isInstanceOf(IllegalStateException::class.java)

    }
}

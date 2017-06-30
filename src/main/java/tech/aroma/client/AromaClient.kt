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

import org.apache.thrift.TException
import org.slf4j.LoggerFactory
import tech.aroma.thrift.application.service.ApplicationService
import tech.aroma.thrift.application.service.SendMessageRequest
import tech.aroma.thrift.authentication.ApplicationToken
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import tech.sirwellington.alchemy.thrift.clients.Clients
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService

/**
 * @author SirWellington
 */
@ThreadSafe
internal class AromaClient : Aroma
{
    private val applicationServiceProvider: Provider<ApplicationService.Iface>
    private val executor: ExecutorService
    private val token: ApplicationToken

    private val operatingSystem = nameOfOS
    private var _hostname = getNetworkName()
    private var _deviceName = getNetworkName()

    internal constructor(@Required applicationServiceProvider: Provider<ApplicationService.Iface>,
                         @Required executor: ExecutorService,
                         @Required token: ApplicationToken)
    {
        checkThat(applicationServiceProvider, executor, token)
                .are(notNull())

        checkThat(token.tokenId)
                .usingMessage("token is missing")
                .isA(nonEmptyString())

        this.applicationServiceProvider = applicationServiceProvider
        this.executor = executor
        this.token = token
    }

    //Publicly facing names
    override var hostname: String
        get() = _hostname
        set(value)
        {
            checkThat(value).isA(nonEmptyString())
            _hostname = value
        }

    override var deviceName: String
        get() = _deviceName
        set(value)
        {
            checkThat(value).isA(nonEmptyString())
            _deviceName = value
        }

    override fun begin(): Aroma.Request
    {
        return RequestImpl(this, "", "", Priority.LOW)
    }

    fun sendMessage(@Required request: RequestImpl)
    {
        val now = System.currentTimeMillis()

        val sendMessageRequest = SendMessageRequest()
                .setApplicationToken(token)
                .setBody(request.text)
                .setTitle(request.title)
                .setUrgency(request.priority.toThrift())
                .setHostname(_hostname)
                .setDeviceName(_deviceName)
                .setOperatingSystemName(operatingSystem)
                .setIpv4Address(ipv4Address)
                .setTimeOfMessage(now)

        executor.submit { sendMessageAsync(sendMessageRequest) }
    }

    private fun sendMessageAsync(request: SendMessageRequest)
    {
        val client = applicationServiceProvider.get()

        checkThat(client)
                .usingMessage("service provider returned null")
                .isA(notNull())

        try
        {
            client.sendMessage(request)
            LOG.debug("Successfully sent message to the Aroma Application Service")
        }
        catch (ex: TException)
        {
            //TODO: Decide if swallowing the exception is appropriate here
            LOG.error("Failed to send message to the Aroma Application Service", ex)
        }
        finally
        {
            Clients.attemptCloseSilently(client)
        }
    }

    private fun getNetworkName(): String
    {
        try
        {
            return InetAddress.getLocalHost().hostName
        }
        catch (ex: UnknownHostException)
        {
            LOG.warn("Could not determine Hostname", ex)
            return ""
        }

    }

    private val ipv4Address: String
        get()
        {
            try
            {
                return InetAddress.getLocalHost().hostAddress
            }
            catch (ex: UnknownHostException)
            {
                LOG.warn("Could not determine IPv4 Address", ex)
                return ""
            }

        }

    private val nameOfOS: String
        get()
        {
            val os = System.getProperty("os.name")
            return os ?: ""
        }

    companion object
    {
        private val LOG = LoggerFactory.getLogger(AromaClient::class.java)
    }

}

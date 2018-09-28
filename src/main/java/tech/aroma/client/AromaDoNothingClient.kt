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

import tech.aroma.client.exceptions.AromaException
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.patterns.SingletonPattern

/**
 * Created by Commander on 4/1/2017.
 */
@Internal
@SingletonPattern
internal class AromaDoNothingClient private constructor() : Aroma
{

    override var hostname: String = ""
    override var deviceName: String = ""
    override var bodyPrefix: String = ""
    override var bodySuffix: String = ""

    override fun begin(): Aroma.Request
    {
        return RequestDoNothing
    }

    /**
     * This inner class is a front for a [Request object][Request] that does absolutely nothing.
     */
    @Internal
    @SingletonPattern
    private object RequestDoNothing : Aroma.Request
    {

        override fun withBody(message: String, vararg args: Any): Aroma.Request
        {
            return this
        }

        override fun titled(title: String): Aroma.Request
        {
            return this
        }

        @Throws(IllegalArgumentException::class)
        override fun withPriority(priority: Priority): Aroma.Request
        {
            return this
        }

        @Throws(IllegalArgumentException::class, AromaException::class)
        override fun send()
        {

        }
    }

    companion object
    {
        val INSTANCE = AromaDoNothingClient()
    }
}

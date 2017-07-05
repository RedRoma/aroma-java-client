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

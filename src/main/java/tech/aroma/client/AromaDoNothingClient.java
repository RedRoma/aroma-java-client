package tech.aroma.client;

import tech.aroma.client.exceptions.AromaException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.SingletonPattern;

/**
 * Created by Commander on 4/1/2017.
 */
@Internal
@SingletonPattern
final class AromaDoNothingClient implements Aroma
{

    static final AromaDoNothingClient INSTANCE = new AromaDoNothingClient();

    private AromaDoNothingClient()
    {

    }


    @Override
    public Request begin()
    {
        return RequestDoNothing.INSTANCE;
    }

    /**
     * This inner class is a front for a {@link Request Request object} that does absolutely nothing.
     */
    @Internal
    @SingletonPattern
    private final static class RequestDoNothing implements Request
    {

        private RequestDoNothing()
        {

        }

        private final static RequestDoNothing INSTANCE = new RequestDoNothing();

        @Override
        public Request withBody(String message, Object... args)
        {
            return this;
        }

        @Override
        public Request titled(String title)
        {
            return this;
        }

        @Override
        public Request withPriority(Priority priority) throws IllegalArgumentException
        {
            return this;
        }

        @Override
        public void send() throws IllegalArgumentException, AromaException
        {

        }
    }
}

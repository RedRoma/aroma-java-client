package tech.aroma.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AlchemyTestRunner.class)
public class AromaDoNothingClientTest
{

    @GenerateString
    private String title;

    @GenerateString
    private String body;

    @GenerateEnum
    private Priority priority;

    private AromaDoNothingClient instance;


    @Before
    public void setup()
    {
        instance = AromaDoNothingClient.INSTANCE;
    }

    @Test
    public void begin() throws Exception
    {
        Aroma.Request request = instance.begin();
        assertThat(request, notNullValue());
    }

    @Test
    public void testSendMessage() throws Exception
    {
        instance.begin()
                .titled(title)
                .withBody(body, body)
                .withPriority(priority);
    }

}
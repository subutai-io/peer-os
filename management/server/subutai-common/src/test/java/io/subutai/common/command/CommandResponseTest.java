package io.subutai.common.command;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class CommandResponseTest
{
    private static final UUID requestId = UUID.randomUUID();
    @Mock
    ResponseImpl response;
    @Mock
    CommandResultImpl commandResult;

    CommandResponse commandResponse;


    @Before
    public void setUp() throws Exception
    {
        commandResponse = new CommandResponse( requestId, response, commandResult );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( requestId, commandResponse.getRequestId() );
        assertEquals( response, commandResponse.getResponse() );
        assertEquals( commandResult, commandResponse.getCommandResult() );
    }
}

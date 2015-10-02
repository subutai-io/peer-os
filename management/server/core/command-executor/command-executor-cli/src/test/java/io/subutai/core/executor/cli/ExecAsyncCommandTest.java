package io.subutai.core.executor.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.executor.api.CommandExecutor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ExecAsyncCommandTest extends SystemOutRedirectTest
{
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String COMMAND = "pwd";
    @Mock
    CommandExecutor commandExecutor;
    @Mock
    CommandResult commandResult;

    ExecAsyncCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ExecAsyncCommand( commandExecutor );
        command.hostId = HOST_ID;
        command.command = COMMAND;
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ExecAsyncCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        //test execution

        command.doExecute();

        verify( commandExecutor )
                .executeAsync( any( String.class ), any( RequestBuilder.class ), isA( CommandCallback.class ) );
    }
}

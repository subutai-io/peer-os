package io.subutai.core.executor.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.executor.api.CommandExecutor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ExecSyncCommandTest extends SystemOutRedirectTest
{
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String COMMAND = "pwd";
    @Mock
    CommandExecutor commandExecutor;
    @Mock
    CommandResult commandResult;

    ExecSyncCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ExecSyncCommand( commandExecutor );
        command.hostId = HOST_ID;
        command.command = COMMAND;
        when( commandExecutor.execute( any( UUID.class ), any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ExecSyncCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        //test execution
        command.doExecute();

        verify( commandExecutor ).execute( any( UUID.class ), any( RequestBuilder.class ) );

        assertThat( getSysOut(), containsString( commandResult.toString() ) );


        //test invalid id

        command.hostId = "invalid id";

        command.doExecute();

        assertThat( getSysOut(), containsString( "Invalid host id" ) );
    }
}

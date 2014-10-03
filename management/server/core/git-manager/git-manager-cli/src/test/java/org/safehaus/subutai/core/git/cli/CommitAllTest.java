package org.safehaus.subutai.core.git.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for CommitAll
 */
public class CommitAllTest
{

    private ByteArrayOutputStream myOut;
    private static final String AGENT_NOT_CONNECTED_MSG = "Agent not connected";
    private static final String COMMIT_ID = "commit id";
    private static final String HOSTNAME = "hostname";
    private static final String ERR_MSG = "OOPS";
    private Agent agent = mock( Agent.class );
    private AgentManager agentManager = mock( AgentManager.class );
    private GitManager gitManager = mock( GitManager.class );


    @Before
    public void setUp()
    {
        when( agentManager.getAgentByHostname( HOSTNAME ) ).thenReturn( agent );
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }


    @After
    public void tearDown()
    {
        System.setOut( System.out );
    }


    private String getSysOut()
    {
        return myOut.toString().trim();
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new CommitAll( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new CommitAll( mock( GitManager.class ), null );
    }


    @Test
    public void shouldFailOnMissingAgent()
    {
        CommitAll commitAll = new CommitAll( mock( GitManager.class ), mock( AgentManager.class ) );

        commitAll.doExecute();

        assertEquals( AGENT_NOT_CONNECTED_MSG, getSysOut() );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {
        CommitAll commitAll = new CommitAll( gitManager, agentManager );
        commitAll.setHostname( HOSTNAME );
        when( gitManager.commitAll( eq( agent ), anyString(), anyString() ) ).thenReturn( COMMIT_ID );

        commitAll.doExecute();

        verify( gitManager ).commitAll( eq( agent ), anyString(), anyString() );
        assertThat( getSysOut(), containsString( COMMIT_ID ) );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .commitAll( eq( agent ), anyString(), anyString() );
        CommitAll commitAll = new CommitAll( gitManager, agentManager );
        commitAll.setHostname( HOSTNAME );

        commitAll.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}

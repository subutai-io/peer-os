package org.safehaus.subutai.core.git.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for AddFiles
 */
public class AddFilesTest
{

    private ByteArrayOutputStream myOut;
    private static final String AGENT_NOT_CONNECTED_MSG = "Agent not connected";
    private static final String REPOSITORY_ROOT = "repo root";
    private static final List<String> FILES = Lists.newArrayList( "file" );
    private static final String HOSTNAME = "hostname";
    private static final String ERR_MSG = "OOPS";


    @Before
    public void setUp()
    {
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
        new AddFiles( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new AddFiles( mock( GitManager.class ), null );
    }


    @Test
    public void shouldFailOnMissingAgent()
    {
        AddFiles addFiles = new AddFiles( mock( GitManager.class ), mock( AgentManager.class ) );

        addFiles.doExecute();

        assertEquals( AGENT_NOT_CONNECTED_MSG, getSysOut() );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( HOSTNAME ) ).thenReturn( agent );
        GitManager gitManager = mock( GitManager.class );
        AddFiles addFiles = new AddFiles( gitManager, agentManager );
        addFiles.setHostname( HOSTNAME );
        addFiles.setRepoPath( REPOSITORY_ROOT );
        addFiles.setFiles( FILES );

        addFiles.doExecute();

        verify( gitManager ).add( agent, REPOSITORY_ROOT, FILES );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( HOSTNAME ) ).thenReturn( agent );
        GitManager gitManager = mock( GitManager.class );
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).add( agent, REPOSITORY_ROOT, FILES );
        AddFiles addFiles = new AddFiles( gitManager, agentManager );
        addFiles.setHostname( HOSTNAME );
        addFiles.setRepoPath( REPOSITORY_ROOT );
        addFiles.setFiles( FILES );

        addFiles.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}

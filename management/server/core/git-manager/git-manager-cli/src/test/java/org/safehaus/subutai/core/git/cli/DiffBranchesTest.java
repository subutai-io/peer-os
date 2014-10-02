package org.safehaus.subutai.core.git.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for DiffBranches
 */
public class DiffBranchesTest
{

    private ByteArrayOutputStream myOut;
    private static final String AGENT_NOT_CONNECTED_MSG = "Agent not connected";
    private static final String HOSTNAME = "hostname";
    private static final String BRANCH_NAME = "branch name";
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
        new DiffBranches( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new DiffBranches( mock( GitManager.class ), null );
    }


    @Test
    public void shouldFailOnMissingAgent()
    {
        DiffBranches diffBranches = new DiffBranches( mock( GitManager.class ), mock( AgentManager.class ) );


        diffBranches.doExecute();


        assertEquals( AGENT_NOT_CONNECTED_MSG, getSysOut() );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        DiffBranches diffBranches = new DiffBranches( gitManager, agentManager );
        diffBranches.setHostname( HOSTNAME );
        when( gitManager.diffBranches( eq( agent ), anyString(), anyString() ) )
                .thenReturn( Lists.newArrayList( mock( GitChangedFile.class ) ) );


        diffBranches.doExecute();


        verify( gitManager ).diffBranches( eq( agent ), anyString(), anyString() );
    }


    @Test
    public void shouldExecuteCommand2() throws GitException
    {

        DiffBranches diffBranches = new DiffBranches( gitManager, agentManager );
        diffBranches.setHostname( HOSTNAME );
        diffBranches.setBranchName2( BRANCH_NAME );


        diffBranches.doExecute();


        verify( gitManager ).diffBranches( eq( agent ), anyString(), anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .diffBranches( eq( agent ), anyString(), anyString() );
        DiffBranches diffBranches = new DiffBranches( gitManager, agentManager );
        diffBranches.setHostname( HOSTNAME );


        diffBranches.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }


    @Test
    public void shouldThrowException2() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .diffBranches( eq( agent ), anyString(), anyString(), anyString() );
        DiffBranches diffBranches = new DiffBranches( gitManager, agentManager );
        diffBranches.setHostname( HOSTNAME );
        diffBranches.setBranchName2( BRANCH_NAME );


        diffBranches.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}

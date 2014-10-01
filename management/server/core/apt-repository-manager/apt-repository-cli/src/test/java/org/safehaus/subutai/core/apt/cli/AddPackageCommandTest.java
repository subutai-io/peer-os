package org.safehaus.subutai.core.apt.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for AddPackageCommand
 */
public class AddPackageCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final String PACKAGE_PATH = "package/path";
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
    public void constructorShouldFailOnNullAptRepoManager()
    {
        new AddPackageCommand( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new AddPackageCommand( mock( AptRepositoryManager.class ), null );
    }


    @Test
    public void shouldAddPackage() throws AptRepoException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ) ).thenReturn( agent );
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );

        AddPackageCommand addPackageCommand = new AddPackageCommand( aptRepositoryManager, agentManager );
        addPackageCommand.setPackagePath( PACKAGE_PATH );
        addPackageCommand.doExecute();

        verify( aptRepositoryManager ).addPackageByPath( eq( agent ), eq( PACKAGE_PATH ), eq( false ) );
    }


    @Test
    public void shouldThrowAptException() throws AptRepoException
    {
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );
        Mockito.doThrow( new AptRepoException( ERR_MSG ) ).when( aptRepositoryManager )
               .addPackageByPath( any( Agent.class ), anyString(), anyBoolean() );

        AddPackageCommand addPackageCommand = new AddPackageCommand( aptRepositoryManager, mock( AgentManager.class ) );
        addPackageCommand.doExecute();

        assertEquals(ERR_MSG, getSysOut());
    }
}

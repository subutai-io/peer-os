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
 * Test for RemovePackageCommand
 */
public class RemovePackageCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final String PACKAGE_NAME = "package name";
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
        new RemovePackageCommand( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new RemovePackageCommand( mock( AptRepositoryManager.class ), null );
    }


    @Test
    public void shouldRemovePackage() throws AptRepoException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ) ).thenReturn( agent );
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );

        RemovePackageCommand removePackageCommand = new RemovePackageCommand( aptRepositoryManager, agentManager );
        removePackageCommand.setPackageName( PACKAGE_NAME );
        removePackageCommand.doExecute();

        verify( aptRepositoryManager ).removePackageByName( eq( agent ), eq( PACKAGE_NAME ) );
    }


    @Test
    public void shouldThrowAptException() throws AptRepoException
    {
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );
        Mockito.doThrow( new AptRepoException( ERR_MSG ) ).when( aptRepositoryManager )
               .removePackageByName( any( Agent.class ), anyString() );

        RemovePackageCommand removePackageCommand = new RemovePackageCommand( aptRepositoryManager, mock( AgentManager.class ) );
        removePackageCommand.doExecute();

        assertEquals(ERR_MSG, getSysOut());
    }
}

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
import org.safehaus.subutai.core.apt.api.PackageInfo;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for AddPackageCommand
 */
public class ListPackagesCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final String PATTERN = "pattern";
    private static final String PACKAGE_INFO_STR = "info";
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
        new ListPackagesCommand( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new ListPackagesCommand( mock( AptRepositoryManager.class ), null );
    }


    @Test
    public void shouldListPackages() throws AptRepoException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ) ).thenReturn( agent );
        PackageInfo packageInfo = mock( PackageInfo.class );
        when( packageInfo.toString() ).thenReturn( PACKAGE_INFO_STR );
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );
        when( aptRepositoryManager.listPackages( agent, PATTERN ) ).thenReturn( Lists.newArrayList( packageInfo ) );

        ListPackagesCommand listPackagesCommand = new ListPackagesCommand( aptRepositoryManager, agentManager );
        listPackagesCommand.setPattern( PATTERN );
        listPackagesCommand.doExecute();

        verify( aptRepositoryManager ).listPackages( eq( agent ), eq( PATTERN ) );
        assertEquals( PACKAGE_INFO_STR, getSysOut() );
    }


    @Test
    public void shouldThrowAptException() throws AptRepoException
    {
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );
        Mockito.doThrow( new AptRepoException( ERR_MSG ) ).when( aptRepositoryManager )
               .listPackages( any( Agent.class ), anyString() );

        ListPackagesCommand listPackagesCommand =
                new ListPackagesCommand( aptRepositoryManager, mock( AgentManager.class ) );
        listPackagesCommand.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}

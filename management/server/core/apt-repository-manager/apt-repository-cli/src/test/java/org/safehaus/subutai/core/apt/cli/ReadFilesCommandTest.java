package org.safehaus.subutai.core.apt.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;

import com.google.common.collect.Lists;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for ReadFilesCommand
 */
public class ReadFilesCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final String PACKAGE_PATH = "path/to/package";
    private static final List<String> FILE_PATHS = Lists.newArrayList( "file path" );
    private static final List<String> FILE_CONTENTS = Lists.newArrayList( "file content" );
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
        new ReadFilesCommand( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new ReadFilesCommand( mock( AptRepositoryManager.class ), null );
    }


    @Test
    public void shouldReadFilesContents() throws AptRepoException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ) ).thenReturn( agent );

        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );
        when( aptRepositoryManager.readFileContents( agent, PACKAGE_PATH, FILE_PATHS ) ).thenReturn( FILE_CONTENTS );

        ReadFilesCommand readFilesCommand = new ReadFilesCommand( aptRepositoryManager, agentManager );
        readFilesCommand.setPackagePath( PACKAGE_PATH );
        readFilesCommand.setFilesPaths( FILE_PATHS );
        readFilesCommand.doExecute();

        verify( aptRepositoryManager ).readFileContents( eq( agent ), eq( PACKAGE_PATH ), eq( FILE_PATHS ) );
        for ( String fileContent : FILE_CONTENTS )
        {
            assertThat( getSysOut(), containsString( fileContent ) );
        }
    }


    @Test
    public void shouldThrowAptException() throws AptRepoException
    {
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );
        Mockito.doThrow( new AptRepoException( ERR_MSG ) ).when( aptRepositoryManager )
               .readFileContents( any( Agent.class ), anyString(), anyList() );

        ReadFilesCommand readFilesCommand =
                new ReadFilesCommand( aptRepositoryManager, mock( AgentManager.class ) );
        readFilesCommand.setFilesPaths( FILE_PATHS );
        readFilesCommand.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}

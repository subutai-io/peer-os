package io.subutai.core.hostregistry.cli;


import java.io.PrintStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.hostregistry.api.ContainerHostInfo;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetContainerHostInfoCommandTest extends SystemOutRedirectTest
{
    private static final String HOSTNAME = "hostname";
    private static final UUID ID = UUID.randomUUID();
    @Mock
    HostRegistry hostRegistry;
    @Mock
    ContainerHostInfo containerHostInfo;

    GetContainerHostInfoCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new GetContainerHostInfoCommand( hostRegistry );
        when( hostRegistry.getContainerHostInfoByHostname( HOSTNAME ) ).thenReturn( containerHostInfo );
        when( hostRegistry.getContainerHostInfoById( ID ) ).thenReturn( containerHostInfo );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new GetContainerHostInfoCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        //check by hostname
        command.identifier = HOSTNAME;

        command.doExecute();

        assertThat( getSysOut(), containsString( containerHostInfo.toString() ) );

        //check by id
        command.identifier = ID.toString();

        command.doExecute();

        assertThat( getSysOut(), containsString( containerHostInfo.toString() ) );

        //check exception

        HostDisconnectedException exception = mock( HostDisconnectedException.class );
        doThrow( exception ).when( hostRegistry ).getContainerHostInfoById( ID );
        resetSysOut();

        command.doExecute();

        assertEquals( "Host is not connected", getSysOut() );
        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}

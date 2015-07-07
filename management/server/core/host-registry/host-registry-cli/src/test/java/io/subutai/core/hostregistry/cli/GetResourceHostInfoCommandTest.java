package io.subutai.core.hostregistry.cli;


import java.io.PrintStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.ResourceHostInfo;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetResourceHostInfoCommandTest extends SystemOutRedirectTest
{
    private static final String HOSTNAME = "hostname";
    private static final UUID ID = UUID.randomUUID();
    @Mock
    HostRegistry hostRegistry;
    @Mock
    ResourceHostInfo resourceHostInfo;

    GetResourceHostInfoCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new GetResourceHostInfoCommand( hostRegistry );
        when( hostRegistry.getResourceHostInfoById( ID ) ).thenReturn( resourceHostInfo );
        when( hostRegistry.getResourceHostInfoByHostname( HOSTNAME ) ).thenReturn( resourceHostInfo );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new GetResourceHostInfoCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        //check by hostname
        command.identifier = HOSTNAME;

        command.doExecute();

        assertThat( getSysOut(), containsString( resourceHostInfo.toString() ) );

        //check by id
        command.identifier = ID.toString();

        command.doExecute();

        assertThat( getSysOut(), containsString( resourceHostInfo.toString() ) );

        //check exception

        HostDisconnectedException exception = mock( HostDisconnectedException.class );
        doThrow( exception ).when( hostRegistry ).getResourceHostInfoById( ID );
        resetSysOut();

        command.doExecute();

        assertEquals( "Host is not connected", getSysOut() );
        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}

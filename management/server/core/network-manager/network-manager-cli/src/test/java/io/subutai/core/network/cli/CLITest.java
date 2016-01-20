package io.subutai.core.network.cli;


import java.io.PrintStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class CLITest extends SystemOutRedirectTest
{
    @Mock
    NetworkManager networkManager;
    @Mock
    NetworkManagerException exception;

    SetupP2PCommand setupP2PCommand;
    SetupTunnelCommand setupTunnelCommand;


    @Test
    public void testRemoveContainerIpCommand() throws Exception
    {
        try
        {
            new RemoveContainerIpCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        RemoveContainerIpCommand removeContainerIpCommand = new RemoveContainerIpCommand( networkManager );

        removeContainerIpCommand.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager ).removeContainerIp( anyString() );

        removeContainerIpCommand.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testRemoveP2PCommand() throws Exception
    {

        try
        {
            new RemoveP2PCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        RemoveP2PCommand removeP2PCommand = new RemoveP2PCommand( networkManager );

        removeP2PCommand.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager ).removeP2PConnection(  anyString() );

        removeP2PCommand.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testRemoveTunnelCommand() throws Exception
    {
        try
        {
            new RemoveTunnelCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        RemoveTunnelCommand removeTunnelCommand = new RemoveTunnelCommand( networkManager );

        removeTunnelCommand.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager ).removeTunnel( anyInt() );

        removeTunnelCommand.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testSetContainerIpCommand() throws Exception
    {
        try
        {
            new SetContainerIpCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        SetContainerIpCommand setContainerIpCommand = new SetContainerIpCommand( networkManager );

        setContainerIpCommand.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager ).setContainerIp( anyString(), anyString(), anyInt(), anyInt() );

        setContainerIpCommand.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testSetP2PCommand() throws Exception
    {
        try
        {
            new SetupP2PCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        SetupP2PCommand command = new SetupP2PCommand( networkManager );

        command.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager )
                            .setupP2PConnection( anyString(), anyInt(), anyString(), anyString(), anyString(),
                                    anyString(), anyString() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testTunnelCommand() throws Exception
    {
        try
        {
            new SetupTunnelCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        SetupTunnelCommand command = new SetupTunnelCommand( networkManager );

        command.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager ).setupTunnel( anyInt(), anyString() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}

package org.safehaus.subutai.core.network.cli;


import java.io.PrintStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;

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

    SetupN2NCommand setupN2NCommand;
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
    public void testRemoveN2NCommand() throws Exception
    {

        try
        {
            new RemoveN2NCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        RemoveN2NCommand removeN2NCommand = new RemoveN2NCommand( networkManager );

        removeN2NCommand.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager ).removeN2NConnection( anyString(), anyString() );

        removeN2NCommand.doExecute();

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


        doThrow( exception ).when( networkManager ).removeTunnel( anyString() );

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
    public void testSetN2NCommand() throws Exception
    {
        try
        {
            new SetupN2NCommand( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }

        SetupN2NCommand command = new SetupN2NCommand( networkManager );

        command.doExecute();

        assertEquals( "OK", getSysOut() );


        doThrow( exception ).when( networkManager )
                            .setupN2NConnection( anyString(), anyInt(), anyString(), anyString(), anyString(),
                                    anyString() , anyString());

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


        doThrow( exception ).when( networkManager ).setupTunnel( anyString(), anyString(), anyString() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}

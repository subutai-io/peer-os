package io.subutai.core.desktop.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.Quota;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.protocol.Template;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerSize;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class DesktopManagerImplTest
{
    private final Logger log = LoggerFactory.getLogger( getClass() );
    public static final String RESULT_DESK = "Unity";
    public static final String RESULT_RDS = "x2goserver";

    private DesktopManagerImpl desktopManager;

    @Mock
    ContainerHost containerHost;


    @Before
    public void setUp() throws Exception
    {
        CommandResult deskCommandResult = new CommandResultImpl( 202, RESULT_DESK, null, CommandStatus.SUCCEEDED );
        CommandResult rdsCommandResult = new CommandResultImpl( 202, RESULT_RDS, null, CommandStatus.SUCCEEDED );
        desktopManager = spy( new DesktopManagerImpl() );
        doReturn( deskCommandResult ).when( containerHost ).execute( Commands.getDeskEnvSpecifyCommand() );
        doReturn( rdsCommandResult ).when( containerHost ).execute( Commands.getRDServerSpecifyCommand() );
    }


    @Test
    @Ignore
    public void isDesktop() throws Exception
    {
        boolean isDekstop = desktopManager.isDesktop( containerHost );
        assertTrue( isDekstop );
    }


    @Test
    public void getDesktopEnvironmentInfo() throws Exception
    {
        String deskEnv = desktopManager.getDesktopEnvironmentInfo( containerHost );
        assertEquals( RESULT_DESK, deskEnv );
    }


    @Test
    public void getRDServerInfo() throws Exception
    {
        String rdServer = desktopManager.getRDServerInfo( containerHost );
        assertEquals( RESULT_RDS, rdServer );
    }
}

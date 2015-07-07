package io.subutai.wol.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.wol.api.WolManagerException;
import io.subutai.wol.impl.Commands;
import io.subutai.wol.impl.WolImpl;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WolImplTest
{
    WolImpl wolImpl;
    PeerManager peerManager;
    Commands commands;
    RequestBuilder requestBuilder;
    LocalPeer localPeer;
    ManagementHost managementHost;
    CommandResult commandResult;
    CommandResult commandResult2;
    @Before
    public void setUp() throws Exception
    {
        commandResult = mock(CommandResult.class);
        commandResult2 = mock(CommandResult.class);
        managementHost = mock(ManagementHost.class);
        localPeer = mock(LocalPeer.class);
        requestBuilder = mock(RequestBuilder.class);
        commands = mock(Commands.class);
        peerManager = mock(PeerManager.class);
        wolImpl = new WolImpl(peerManager);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowsNullPointerExceptionInConstructor()
    {
        wolImpl = new WolImpl(null);
    }

    @Test
    public void testSendMagicPackageByMacId() throws Exception
    {
        // mock Commands.class GetSendWakeOnLanCommand() method
        when(commands.getSendWakeOnLanCommand(anyString())).thenReturn(requestBuilder);

        // mock getManagementHost() method
        when(peerManager.getLocalPeer()).thenReturn(localPeer);
        when(localPeer.getManagementHost()).thenReturn(managementHost);

        // mock execute method
        when(managementHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);

        wolImpl.sendMagicPackageByMacId("test");

        // assertions
        assertNotNull(commands.getSendWakeOnLanCommand("test"));
        assertNotNull(localPeer.getManagementHost());
        assertTrue(commandResult.hasSucceeded());
        assertNotNull(commandResult);
        verify(commands).getSendWakeOnLanCommand(anyString());
    }

    @Test
    public void testSendMagicPackageByList() throws Exception
    {
        ArrayList<String> myList = new ArrayList<>();
        myList.add("test");

        // mock Commands.class GetSendWakeOnLanCommand() method
        when(commands.getSendWakeOnLanCommand(anyString())).thenReturn(requestBuilder);

        // mock getManagementHost() method
        when(peerManager.getLocalPeer()).thenReturn(localPeer);
        when(localPeer.getManagementHost()).thenReturn(managementHost);

        // mock execute method
        when(managementHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);

        wolImpl.sendMagicPackageByList(myList);

        // assertions
        assertNotNull(commands.getSendWakeOnLanCommand("test"));
        assertNotNull(localPeer.getManagementHost());
        assertTrue(commandResult.hasSucceeded());
        assertNotNull(commandResult);
        verify(commands).getSendWakeOnLanCommand(anyString());
        assertTrue(wolImpl.sendMagicPackageByList(myList));
    }

    @Test(expected = WolManagerException.class)
    public void shouldThrowsWolManagerExceptionInMethodSendMagicPackageByList() throws Exception
    {
        ArrayList<String> myList = new ArrayList<>();
        myList.add("test");

        // mock Commands.class GetSendWakeOnLanCommand() method
        when(commands.getSendWakeOnLanCommand(anyString())).thenReturn(requestBuilder);

        // mock getManagementHost() method
        when(peerManager.getLocalPeer()).thenReturn(localPeer);
        when(localPeer.getManagementHost()).thenReturn(managementHost);

        // mock execute method
        when(managementHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(false);

        wolImpl.sendMagicPackageByList(myList);

        // assertions
        assertNotNull(commands.getSendWakeOnLanCommand("test"));
        assertNotNull(localPeer.getManagementHost());
        assertNotNull(commandResult);
        verify(commands).getSendWakeOnLanCommand(anyString());
        assertFalse(wolImpl.sendMagicPackageByList(myList));
    }

    @Test(expected = WolManagerException.class)
    public void shouldThrowsWolManagerExceptionInMethodSendMagicPackageByMacId() throws Exception
    {
        // mock Commands.class GetSendWakeOnLanCommand() method
        when(commands.getSendWakeOnLanCommand(anyString())).thenReturn(requestBuilder);

        // mock getManagementHost() method
        when(peerManager.getLocalPeer()).thenReturn(localPeer);
        when(localPeer.getManagementHost()).thenReturn(managementHost);

        // mock execute method
        when(managementHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(false);

        wolImpl.sendMagicPackageByMacId("test");

        // assertions
        assertNotNull(commands.getSendWakeOnLanCommand("test"));
        assertNotNull(localPeer.getManagementHost());
        assertFalse(commandResult.hasSucceeded());
        assertNotNull(commandResult);
        verify(commands).getSendWakeOnLanCommand(anyString());
    }
}
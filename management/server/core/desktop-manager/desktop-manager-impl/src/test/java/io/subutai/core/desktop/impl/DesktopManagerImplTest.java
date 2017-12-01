package io.subutai.core.desktop.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.peer.api.PeerManager;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class DesktopManagerImplTest
{
    private final static String CONT_ID = "..."; //TODO;
    private DesktopManagerImpl desktopManager;

    @Mock
    PeerManager peerManager;


    @Before
    public void setUp() throws Exception
    {
        desktopManager = spy( new DesktopManagerImpl() );
        //TODO add return cases for peerManager.getLocalPeer()..
    }


    @Test
    @Ignore
    public void isDesktop() throws Exception
    {
        ContainerHost containerHost = peerManager.getLocalPeer().getContainerHostById( CONT_ID );
        boolean isDekstop = desktopManager.isDesktop( containerHost );

        assertTrue( isDekstop );
    }


    @Test
    public void getDesktopEnvironmentInfo() throws Exception
    {

    }


    @Test
    public void getRDServerInfo() throws Exception
    {

    }
}

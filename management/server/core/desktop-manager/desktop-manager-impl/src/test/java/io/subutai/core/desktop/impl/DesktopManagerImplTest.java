package io.subutai.core.desktop.impl;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.peer.api.PeerManager;


@RunWith( MockitoJUnitRunner.class )
public class DesktopManagerImplTest
{

    private DesktopManagerImpl desktopManager;

    @Mock
    PeerManager peerManager;


    @Test
    public void isDesktop() throws Exception
    {
        boolean isDekstop = desktopManager.isDesktop( null );
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

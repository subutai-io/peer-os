package io.subutai.core.desktop.impl;


import io.subutai.common.peer.ContainerHost;
import io.subutai.core.desktop.api.DesktopManager;


public class DesktopManagerImpl implements DesktopManager
{
    @Override
    public boolean isDesktop( final ContainerHost containerHost )
    {
        return false;
    }


    @Override
    public String getDesktopEnvironment( final ContainerHost containerHost )
    {
        return null;
    }


    @Override
    public String getRDServer( final ContainerHost containerHost )
    {
        return null;
    }
}

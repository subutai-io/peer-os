package io.subutai.core.desktop.impl;


import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.desktop.api.DesktopManager;
import io.subutai.core.peer.api.PeerManager;


public class DesktopManagerImpl implements DesktopManager
{
    private final PeerManager peerManager;


    public DesktopManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );
        this.peerManager = peerManager;
    }


    @Override
    public boolean isDesktop( final ContainerHost containerHost ) throws CommandException
    {
        String result = getDesktopEnvironmentInfo( containerHost );
        if ( result != null )
        {
            return true;
        }

        return false;
    }


    @Override
    public String getDesktopEnvironmentInfo( final ContainerHost containerHost ) throws CommandException
    {
        CommandResult result = containerHost.execute( Commands.getDeskEnvSpecifyCommand() );
        return result.getStdOut();
    }


    @Override
    public String getRDServerInfo( final ContainerHost containerHost ) throws CommandException
    {
        CommandResult result = containerHost.execute( Commands.getRDServerSpecifyCommand() );
        return result.getStdOut();
    }
}

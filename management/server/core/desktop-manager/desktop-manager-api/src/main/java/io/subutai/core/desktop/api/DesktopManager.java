package io.subutai.core.desktop.api;


import io.subutai.common.command.CommandException;
import io.subutai.common.peer.ContainerHost;


/**
 * Exposes API to work with desktop container hosts.
 */
public interface DesktopManager
{
    boolean isDesktop( ContainerHost containerHost ) throws CommandException;

    /**
     * @return desktop environment (DE) information
     */
    String getDesktopEnvironmentInfo( ContainerHost containerHost ) throws CommandException;

    /**
     * @return remote desktop server information, ex: X2Go Server
     */
    String getRDServerInfo( ContainerHost containerHost ) throws CommandException;
}

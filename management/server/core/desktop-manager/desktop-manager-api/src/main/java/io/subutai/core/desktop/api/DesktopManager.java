package io.subutai.core.desktop.api;


import java.util.concurrent.ConcurrentMap;

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

    /**
     * copies authorized keys to x2go client user
     */
    void createSSHDir( ContainerHost containerHost ) throws CommandException;

    /**
     * copies authorized keys to x2go client user
     */
    void copyKeys( ContainerHost containerHost ) throws CommandException;

    /**
     * creates default desktop user for Remote Desktop client
     */
    void createDesktopUser( ContainerHost containerHost ) throws CommandException;

    boolean existInCache( String containerId );

    void containerIsDesktop( String containerId );

    void containerIsNotDesktop( String containerId );

    /**
     * invalidated cache of give container host
     */
    void invalidate( String containerId );
}

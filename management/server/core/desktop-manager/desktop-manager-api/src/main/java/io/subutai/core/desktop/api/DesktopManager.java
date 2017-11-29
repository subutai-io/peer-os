package io.subutai.core.desktop.api;


import io.subutai.common.peer.ContainerHost;


/**
 * Exposes API to work with desktop container hosts.
 */
public interface DesktopManager
{
    boolean isDesktop( ContainerHost containerHost );

    /**
     * @return desktop environment (DE) information
     */
    String getDesktopEnvironmentInfo( ContainerHost containerHost );

    /**
     * @return remote desktop server information, ex: X2Go Server
     */
    String getRDServerInfo( ContainerHost containerHost );
}

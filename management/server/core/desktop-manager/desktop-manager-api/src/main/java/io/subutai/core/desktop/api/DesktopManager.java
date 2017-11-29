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
    String getDesktopEnvironment( ContainerHost containerHost );

    /**
     * @return remote desktop server information, ex: X2Go Server
     */
    String getRDServer( ContainerHost containerHost );
}

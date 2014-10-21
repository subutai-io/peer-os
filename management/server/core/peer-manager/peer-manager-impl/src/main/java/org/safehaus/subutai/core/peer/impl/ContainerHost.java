package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.Host;


/**
 * ContainerHost implementation.
 */
public class ContainerHost extends Host
{
    @Override
    public String getHostname()
    {
        return getAgent() == null ? "Unknown container host" : getAgent().getHostname();
    }
}

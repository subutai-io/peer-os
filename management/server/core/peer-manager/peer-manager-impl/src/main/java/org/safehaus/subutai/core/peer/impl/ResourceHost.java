package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.Host;


/**
 * Resource host implementation.
 */
public class ResourceHost extends Host
{
    @Override
    public String getHostname()
    {
        return getAgent() == null ? "Unknown resource host" : getAgent().getHostname();
    }
}

package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.Host;


/**
 * Management host implementation.
 */
public class ManagementHost extends Host
{
    private static final String DEFAULT_MANAGEMENT_HOSTNAME = "management";


    @Override
    public String getHostname()
    {
        return DEFAULT_MANAGEMENT_HOSTNAME;
    }
}

package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;

import com.google.common.base.Objects;


/**
 * Implementation of ContainerHostInfo
 */
public class ContainerHostInfoImpl implements ContainerHostInfo
{
    private UUID id;
    private String hostname;
    private Set<String> ips;
    private ContainerHostState status;


    @Override
    public UUID getId()
    {
        return id;
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public Set<String> getIps()
    {
        return ips;
    }


    @Override
    public ContainerHostState getStatus()
    {
        return status;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname ).add( "ips", ips )
                      .add( "status", status ).toString();
    }
}

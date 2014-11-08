package org.safehaus.subutai.core.containerregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.containerregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.containerregistry.api.HostInfo;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;


/**
 * Implementation of HostInfo
 */
public class HostInfoImpl implements HostInfo
{
    UUID id;
    String hostname;
    Set<String> ips;
    String macAddress;
    Set<ContainerHostInfoImpl> containers;


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
    public String getMacAddress()
    {
        return macAddress;
    }


    @Override
    public Set<ContainerHostInfo> getContainers()
    {
        Set<ContainerHostInfo> info = Sets.newHashSet();

        info.addAll( containers );

        return info;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname ).add( "ips", ips )
                      .add( "macAddress", macAddress ).add( "containers", containers ).toString();
    }
}

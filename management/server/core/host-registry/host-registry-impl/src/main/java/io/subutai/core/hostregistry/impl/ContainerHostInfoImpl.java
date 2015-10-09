package io.subutai.core.hostregistry.impl;


import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.common.util.CollectionUtil;


/**
 * Implementation of ContainerHostInfo
 */
public class ContainerHostInfoImpl implements ContainerHostInfo
{
    private String id;
    private String hostname;
    private String containerName;
    private Set<InterfaceImpl> interfaces;
    private ContainerHostState status;
    private HostArchitecture arch;


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public Set<Interface> getInterfaces()
    {
        Set<Interface> result = Sets.newHashSet();
        if ( !CollectionUtil.isCollectionEmpty( interfaces ) )
        {
            result.addAll( interfaces );
        }
        return result;
    }


    @Override
    public ContainerHostState getStatus()
    {
        return status;
    }


    @Override
    public String getContainerName()
    {
        return containerName;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname )
                          .add( "containerName", containerName ).add( "interfaces", interfaces ).add( "status", status )
                          .add( "arch", arch ).toString();
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        if ( hostname != null && o != null )
        {
            return hostname.compareTo( o.getHostname() );
        }
        return -1;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ContainerHostInfoImpl ) )
        {
            return false;
        }

        final ContainerHostInfoImpl that = ( ContainerHostInfoImpl ) o;

        if ( id != null ? !id.equals( that.id ) : that.id != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}

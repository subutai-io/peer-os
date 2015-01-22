package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;


/**
 * Implementation of ContainerHostInfo
 */
public class ContainerHostInfoImpl implements ContainerHostInfo
{
    private UUID id;
    private String hostname;
    private Set<InterfaceImpl> interfaces;
    private ContainerHostState status;
    private HostArchitecture arch;


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
    public HostArchitecture getArch()
    {
        return arch;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname )
                      .add( "interfaces", interfaces ).add( "status", status ).add( "arch", arch ).toString();
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

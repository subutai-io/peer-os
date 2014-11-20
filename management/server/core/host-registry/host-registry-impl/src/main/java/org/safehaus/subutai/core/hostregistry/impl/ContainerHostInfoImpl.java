package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.Interface;

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
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname )
                      .add( "interfaces", interfaces ).add( "status", status ).toString();
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        return 0;
    }
}

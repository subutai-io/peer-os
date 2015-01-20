package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;


/**
 * Implementation of ResourceHostInfo
 */
public class ResourceHostInfoImpl implements ResourceHostInfo
{
    private UUID id;
    private String hostname;
    private Set<InterfaceImpl> interfaces;
    private Set<ContainerHostInfoImpl> containers;
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
    public Set<ContainerHostInfo> getContainers()
    {
        Set<ContainerHostInfo> result = Sets.newHashSet();

        if ( !CollectionUtil.isCollectionEmpty( containers ) )
        {
            //TODO remove this temp workaround when agent is fixed
            for ( ContainerHostInfoImpl containerHostInfo : containers )
            {
                if ( !CollectionUtil.isCollectionEmpty( containerHostInfo.getInterfaces() ) )
                {
                    result.add( containerHostInfo );
                }
            }
            //TODO

            //            result.addAll( containers );
        }

        return result;
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
                      .add( "interfaces", interfaces ).add( "containers", containers ).toString();
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
        if ( !( o instanceof ResourceHostInfoImpl ) )
        {
            return false;
        }

        final ResourceHostInfoImpl that = ( ResourceHostInfoImpl ) o;

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

package io.subutai.core.security.broker;


import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ResourceHostInfo;


/**
 * Implementation of ResourceHostInfo
 */
public class ResourceHostInfoImpl implements ResourceHostInfo
{
    private String id;
    private String hostname;
    private Set<InterfaceImpl> interfaces;
    private Set<ContainerHostInfoImpl> containers;
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
    public Set<ContainerHostInfo> getContainers()
    {
        Set<ContainerHostInfo> result = Sets.newHashSet();

        if ( !CollectionUtil.isCollectionEmpty( containers ) )
        {
            result.addAll( containers );
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

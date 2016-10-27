package io.subutai.common.network;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class ReservedNetworkResources
{
    @JsonProperty( "networkResources" )
    Set<NetworkResource> networkResources = Sets.newHashSet();


    public ReservedNetworkResources( @JsonProperty( "networkResources" ) final Set<NetworkResource> networkResources )
    {
        Preconditions.checkNotNull( networkResources );

        this.networkResources = networkResources;
    }


    public ReservedNetworkResources()
    {
    }


    public void addNetworkResource( NetworkResource networkResource )
    {
        this.networkResources.add( networkResource );
    }


    public Set<NetworkResource> getNetworkResources()
    {
        return networkResources;
    }


    public NetworkResource findByEnvironmentId( String environmentId )
    {
        for ( NetworkResource networkResource : networkResources )
        {
            if ( networkResource.getEnvironmentId().equalsIgnoreCase( environmentId ) )
            {
                return networkResource;
            }
        }

        return null;
    }


    public NetworkResource findByVlan( int vlan )
    {
        for ( NetworkResource networkResource : networkResources )
        {
            if ( networkResource.getVlan() == vlan )
            {
                return networkResource;
            }
        }

        return null;
    }


    public NetworkResource findByVni( long vni )
    {
        for ( NetworkResource networkResource : networkResources )
        {
            if ( networkResource.getVni() == vni )
            {
                return networkResource;
            }
        }

        return null;
    }
}

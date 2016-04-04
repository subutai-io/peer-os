package io.subutai.common.protocol;


import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.util.CollectionUtil;


public class Tunnels
{
    @JsonProperty( "tunnels" )
    private Set<Tunnel> tunnels = Sets.newHashSet();


    public Tunnels( @JsonProperty( "tunnels" ) final Set<Tunnel> tunnels )
    {
        Preconditions.checkNotNull( tunnels );

        this.tunnels = tunnels;
    }


    public Tunnels()
    {
    }


    public Tunnel findByIp( String ip )
    {
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelIp().equals( ip ) )
            {
                return tunnel;
            }
        }

        return null;
    }


    public Tunnel findByName( String name )
    {
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelName().equalsIgnoreCase( name ) )
            {
                return tunnel;
            }
        }

        return null;
    }


    public void addTunnel( Tunnel tunnel )
    {
        Preconditions.checkNotNull( tunnel );

        tunnels.add( tunnel );
    }


    public Set<Tunnel> getTunnels()
    {
        return tunnels;
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtil.isCollectionEmpty( tunnels );
    }
}

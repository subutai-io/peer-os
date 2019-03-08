package io.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * P2P helper class
 */
public class P2PConnections
{
    @JsonProperty( "connections" )
    private Set<P2PConnection> connections = new HashSet<>();


    public P2PConnections( @JsonProperty( "connections" ) final Set<P2PConnection> connections )
    {
        Preconditions.checkNotNull( connections );

        this.connections = connections;
    }


    public P2PConnections()
    {
    }


    public void addConnection( P2PConnection p2PConnection )
    {

        Preconditions.checkNotNull( p2PConnection, "P2P connection can not be null." );

        connections.add( p2PConnection );
    }


    public Set<P2PConnection> getConnections()
    {
        return this.connections;
    }


    public P2PConnection findByHash( final String hash )
    {
        Preconditions.checkNotNull( hash );

        for ( P2PConnection p2PConnection : getConnections() )
        {
            if ( p2PConnection.getHash().equalsIgnoreCase( hash ) )
            {
                return p2PConnection;
            }
        }

        return null;
    }


    public P2PConnection findByIp( final String ip )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( ip ) );

        for ( P2PConnection p2PConnection : getConnections() )
        {
            if ( p2PConnection.getIp().equalsIgnoreCase( ip ) )
            {
                return p2PConnection;
            }
        }

        return null;
    }
}

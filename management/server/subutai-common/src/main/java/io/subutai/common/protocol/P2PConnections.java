package io.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.util.CollectionUtil;


/**
 * P2P helper class
 */
public class P2PConnections
{
    @JsonProperty( "connections" )
    private Set<P2PConnection> connections = new HashSet<>();


    public P2PConnections( @JsonProperty( "connections" ) final Set<P2PConnection> connections )
    {
        this.connections = connections;
    }


    public P2PConnections()
    {
    }


    public void addConnection( P2PConnection p2PConnection )
    {
        if ( p2PConnection == null )
        {
            throw new IllegalArgumentException( "P2P connection can not be null." );
        }
        connections.add( p2PConnection );
    }


    public Set<P2PConnection> getConnections()
    {
        return CollectionUtil.isCollectionEmpty( connections ) ? Sets.<P2PConnection>newHashSet() : this.connections;
    }


    public P2PConnection findConnectionByHash( final String hash )
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
}

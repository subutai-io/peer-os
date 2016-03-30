package io.subutai.common.protocol;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Preconditions;


/**
 * P2P helper class
 */
public class P2PConnections
{
    private Set<P2PConnection> connections = new HashSet<>();


    public P2PConnections( final Set<P2PConnection> connections )
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
            throw new IllegalArgumentException( "P2P connection could not be null." );
        }
        connections.add( p2PConnection );
    }


    public Set<P2PConnection> getConnections()
    {
        return this.connections == null ? new HashSet<P2PConnection>() : this.connections;
    }


    public P2PConnection findConnectionByHash( final String hash )
    {
        Preconditions.checkNotNull( hash );

        P2PConnection result = null;
        for ( Iterator<P2PConnection> i = connections.iterator(); i.hasNext() && result == null; )
        {
            P2PConnection c = i.next();
            if ( hash.equalsIgnoreCase( c.getHash() ) )
            {
                result = c;
            }
        }
        return result;
    }
}

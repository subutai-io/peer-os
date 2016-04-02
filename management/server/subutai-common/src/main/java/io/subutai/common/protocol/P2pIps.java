package io.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.util.CollectionUtil;


public class P2pIps
{
    @JsonProperty( "p2pIps" )
    Set<RhP2pIp> p2pIps = new HashSet<>();


    public P2pIps( @JsonProperty( "p2pIps" ) Set<RhP2pIp> p2pIps )
    {
        Preconditions.checkNotNull( p2pIps );

        this.p2pIps = p2pIps;
    }


    public P2pIps()
    {
    }


    public Set<RhP2pIp> getP2pIps()
    {
        return p2pIps;
    }


    public void addP2pIps( Set<RhP2pIp> p2pIps )
    {
        Preconditions.checkNotNull( p2pIps );

        this.p2pIps.addAll( p2pIps );
    }


    public RhP2pIp findByRhId( String rhId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ) );

        for ( RhP2pIp rhP2pIp : p2pIps )
        {
            if ( rhP2pIp.getRhId().equalsIgnoreCase( rhId ) )
            {
                return rhP2pIp;
            }
        }

        return null;
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtil.isCollectionEmpty( p2pIps );
    }
}


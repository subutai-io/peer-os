package io.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

import io.subutai.common.util.CollectionUtil;


public class P2pIps
{
    @JsonProperty( "p2pIps" )
    Set<String> p2pIps = new HashSet<>();


    public P2pIps( @JsonProperty( "p2pIps" ) Set<String> p2pIps )
    {
        this.p2pIps = p2pIps;
    }


    public P2pIps()
    {
    }


    public Set<String> getP2pIps()
    {
        return p2pIps;
    }


    public void addP2pIps( Set<String> p2pIps )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( p2pIps ) );

        this.p2pIps.addAll( p2pIps );
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtil.isCollectionEmpty( p2pIps );
    }
}


package io.subutai.common.network;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;


public class Gateways
{
    @JsonProperty( "gateways" )
    private Set<Gateway> gateways = new HashSet<>();


    public Gateways( @JsonProperty( "gateways" ) final Set<Gateway> gateways )
    {
        this.gateways = gateways;
    }


    public Gateways()
    {
    }


    public Set<Gateway> list()
    {
        return gateways;
    }


    public void add( final Gateway gateway )
    {
        if ( gateway == null )
        {
            throw new IllegalArgumentException( "Gateway could not be null." );
        }

        this.gateways.add( gateway );
    }


    public Gateway findGatewayByIp( final String ip )
    {
        for ( Gateway gateway : gateways )
        {
            if ( gateway.getIp().equals( ip ) )
            {
                return gateway;
            }
        }

        return null;
    }
}

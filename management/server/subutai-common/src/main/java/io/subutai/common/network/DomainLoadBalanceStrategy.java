package io.subutai.common.network;


public enum DomainLoadBalanceStrategy
{
    ROUND_ROBIN( "rr" ),
    LOAD_BALANCE( "lb" ),
    STICKY_SESSION( "hash" );

    private final String value;


    DomainLoadBalanceStrategy( final String value )
    {
        this.value = value;
    }


    public String getValue()
    {
        return value;
    }
}

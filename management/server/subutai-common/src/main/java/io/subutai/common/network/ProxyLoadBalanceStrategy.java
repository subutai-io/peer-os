package io.subutai.common.network;


public enum ProxyLoadBalanceStrategy
{
    NONE( "none" ),
    ROUND_ROBIN( "rr" ),
    LOAD_BALANCE( "lb" ),
    STICKY_SESSION( "hash" );

    private final String value;


    ProxyLoadBalanceStrategy( final String value )
    {
        this.value = value;
    }


    public String getValue()
    {
        return value;
    }
}

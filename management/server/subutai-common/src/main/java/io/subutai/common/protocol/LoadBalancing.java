package io.subutai.common.protocol;


public enum LoadBalancing
{
    ROUND_ROBIN( "rr" ), IP_HASH( "sticky" ), LEAST_CONN( "lcon" );

    private String policyName;


    LoadBalancing( final String policyName )
    {
        this.policyName = policyName;
    }


    public String getPolicyName()
    {
        return policyName;
    }
}

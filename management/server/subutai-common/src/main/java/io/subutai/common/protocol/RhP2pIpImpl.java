package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.environment.RhP2pIp;


public class RhP2pIpImpl implements RhP2pIp
{
    @JsonProperty( "rhId" )
    private String rhId;
    @JsonProperty( "p2pIp" )
    private String p2pIp;


    public RhP2pIpImpl( @JsonProperty( "rhId" ) final String rhId, @JsonProperty( "p2pIp" ) final String p2pIp )
    {
        this.rhId = rhId;
        this.p2pIp = p2pIp;
    }


    @Override
    public String getRhId()
    {
        return rhId;
    }


    @Override
    public String getP2pIp()
    {
        return p2pIp;
    }
}

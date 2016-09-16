package io.subutai.common.protocol;


import com.fasterxml.jackson.annotation.JsonProperty;

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


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final RhP2pIpImpl rhP2pIp = ( RhP2pIpImpl ) o;

        if ( p2pIp != null ? !p2pIp.equals( rhP2pIp.p2pIp ) : rhP2pIp.p2pIp != null )
        {
            return false;
        }
        if ( rhId != null ? !rhId.equals( rhP2pIp.rhId ) : rhP2pIp.rhId != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = rhId != null ? rhId.hashCode() : 0;
        result = 31 * result + ( p2pIp != null ? p2pIp.hashCode() : 0 );
        return result;
    }
}

package io.subutai.core.bazaarmanager.impl.model;


import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.settings.Common;


@Embeddable
public class RhP2PIpEntity implements RhP2pIp
{
    @JsonProperty( "rhId" )
    @Column( name = "rh_id", nullable = false )
    private String rhId;

    @JsonProperty( "p2pIp" )
    @Column( name = "p2p_ip", nullable = false )
    private String p2pIp;


    public RhP2PIpEntity( @JsonProperty( "rhId" ) final String rhId, @JsonProperty( "p2pIp" ) final String p2pIp )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ), "Resource host id is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pIp ), "Resource host IP is null or empty" );
        Preconditions.checkArgument( p2pIp.matches( Common.IP_REGEX ) );

        this.rhId = rhId;
        this.p2pIp = p2pIp;
    }


    public RhP2PIpEntity() {}


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

        final RhP2PIpEntity rhP2pIp = ( RhP2PIpEntity ) o;

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

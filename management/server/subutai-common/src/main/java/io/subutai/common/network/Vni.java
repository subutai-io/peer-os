package io.subutai.common.network;


import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;


public class Vni
{
    private final long vni;
    private final String environmentId;
    private int vlan = Common.MIN_VLAN_ID - 1;


    public Vni( final long vni, final int vlan, final String environmentId )
    {
        this( vni, environmentId );

        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                String.format( "Vlan must be in range %d - %d", Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        this.vlan = vlan;
    }


    public Vni( final long vni, final String environmentId )
    {
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ),
                String.format( "Vni id must be in range %d - %d", Common.MIN_VNI_ID, Common.MAX_VNI_ID ) );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        this.vni = vni;
        this.environmentId = environmentId;
    }


    public int getVlan()
    {
        return vlan;
    }


    public long getVni()
    {
        return vni;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "vni", vni ).add( "environmentId", environmentId )
                      .add( "vlan", vlan ).toString();
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Vni ) )
        {
            return false;
        }

        final Vni vni1 = ( Vni ) o;

        if ( vlan != vni1.vlan )
        {
            return false;
        }
        if ( vni != vni1.vni )
        {
            return false;
        }
        if ( !environmentId.equals( vni1.environmentId ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = ( int ) ( vni ^ ( vni >>> 32 ) );
        result = 31 * result + environmentId.hashCode();
        result = 31 * result + vlan;
        return result;
    }
}

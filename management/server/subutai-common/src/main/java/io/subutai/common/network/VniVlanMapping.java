package io.subutai.common.network;


import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;


public class VniVlanMapping
{
    private final int tunnelId;
    private final long vni;
    private final int vlan;
    private final String environmentId;


    public VniVlanMapping( final int tunnelId, final long vni, final int vlan, final String environmentId )
    {
        Preconditions.checkNotNull( environmentId );

        this.tunnelId = tunnelId;
        this.vni = vni;
        this.vlan = vlan;
        this.environmentId = environmentId;
    }


    public int getTunnelId()
    {
        return tunnelId;
    }


    public long getVni()
    {
        return vni;
    }


    public int getVlan()
    {
        return vlan;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof VniVlanMapping ) )
        {
            return false;
        }

        final VniVlanMapping mapping = ( VniVlanMapping ) o;

        if ( tunnelId != mapping.tunnelId )
        {
            return false;
        }
        if ( vlan != mapping.vlan )
        {
            return false;
        }
        if ( vni != mapping.vni )
        {
            return false;
        }
        if ( !environmentId.equals( mapping.environmentId ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = tunnelId;
        result = 31 * result + ( int ) ( vni ^ ( vni >>> 32 ) );
        result = 31 * result + vlan;
        result = 31 * result + environmentId.hashCode();
        return result;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "tunnelId", tunnelId ).add( "vni", vni ).add( "vlan", vlan )
                          .add( "environmentId", environmentId ).toString();
    }
}

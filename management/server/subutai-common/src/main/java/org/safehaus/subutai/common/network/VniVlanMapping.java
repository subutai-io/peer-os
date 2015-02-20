package org.safehaus.subutai.common.network;


import com.google.common.base.Objects;


public class VniVlanMapping
{
    private final int tunnelId;
    private final long vni;
    private final int vlan;


    public VniVlanMapping( final int tunnelId, final long vni, final int vlan )
    {
        this.tunnelId = tunnelId;
        this.vni = vni;
        this.vlan = vlan;
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


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "tunnelId", tunnelId ).add( "vni", vni ).add( "vlan", vlan )
                      .toString();
    }
}

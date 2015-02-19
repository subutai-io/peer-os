package org.safehaus.subutai.common.network;


public class VniVlanMapping
{
    private final String tunnelName;
    private final long vni;
    private final int vlan;


    public VniVlanMapping( final String tunnelName, final long vni, final int vlan )
    {
        this.tunnelName = tunnelName;
        this.vni = vni;
        this.vlan = vlan;
    }


    public String getTunnelName()
    {
        return tunnelName;
    }


    public long getVni()
    {
        return vni;
    }


    public int getVlan()
    {
        return vlan;
    }
}

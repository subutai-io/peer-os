package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.core.network.api.VniVlanMapping;


public class VniVlanMappingImpl implements VniVlanMapping
{

    private final String tunnelName;
    private final long vni;
    private final int vlan;


    public VniVlanMappingImpl( final String tunnelName, final long vni, final int vlan )
    {
        this.tunnelName = tunnelName;
        this.vni = vni;
        this.vlan = vlan;
    }


    @Override
    public String getTunnelName()
    {
        return tunnelName;
    }


    @Override
    public long getVni()
    {
        return vni;
    }


    @Override
    public int getVlan()
    {
        return vlan;
    }
}

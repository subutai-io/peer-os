package org.safehaus.subutai.common.network;


public class Gateway
{
    private int vlan;
    private String ip;


    public Gateway( final int vlan, final String ip )
    {
        this.vlan = vlan;
        this.ip = ip;
    }


    public int getVlan()
    {
        return vlan;
    }


    public String getIp()
    {
        return ip;
    }
}

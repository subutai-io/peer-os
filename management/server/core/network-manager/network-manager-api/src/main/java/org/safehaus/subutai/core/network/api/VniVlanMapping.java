package org.safehaus.subutai.core.network.api;


public interface VniVlanMapping
{
    public String getTunnelName();

    public long getVni();

    public int getVlan();
}

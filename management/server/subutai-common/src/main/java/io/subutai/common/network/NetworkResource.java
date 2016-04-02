package io.subutai.common.network;


public interface NetworkResource
{
    public String getEnvironmentId();


    public long getVni();


    public String getP2pSubnet();


    public String getContainerSubnet();

    public int getVlan();
}

package io.subutai.common.peer;


public interface NetworkResource
{
    public String getEnvironmentId();


    public long getVni();


    public String getP2pSubnet();


    public String getContainerSubnet();
}

package io.subutai.common.network;


public interface NetworkResource
{
    String getEnvironmentId();

    long getVni();

    String getP2pSubnet();

    String getContainerSubnet();

    int getVlan();

    String getInitiatorPeerId();

    String getUsername();

    String getUserId();
}

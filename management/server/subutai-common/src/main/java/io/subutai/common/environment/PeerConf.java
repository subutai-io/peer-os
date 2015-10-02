package io.subutai.common.environment;


public interface PeerConf
{
    String getPeerId();

    Environment getEnvironment();

    void setEnvironment( Environment environment );

    String getTunnelAddress();
}

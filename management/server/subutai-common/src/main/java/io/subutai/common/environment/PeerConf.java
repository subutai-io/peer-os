package io.subutai.common.environment;


import java.util.Set;


public interface PeerConf
{
    String getPeerId();

    Environment getEnvironment();

    void setEnvironment( Environment environment );

    Set<String> getP2pIps();

    void addP2pIps( Set<String> p2pIps );
}

package io.subutai.common.environment;


import java.io.Serializable;
import java.util.Set;


public interface EnvironmentPeer extends Serializable
{
    String getPeerId();

    Integer getVlan();

    Environment getEnvironment();

    void setEnvironment( Environment environment );

    void addRhP2pIps( Set<RhP2pIp> rhP2pIps );

    Set<RhP2pIp> getRhP2pIps();
}

package io.subutai.common.environment;


import java.io.Serializable;
import java.util.Set;


public interface EnvironmentPeer extends Serializable
{
    String getPeerId();

    Integer getVlan();

    Environment getEnvironment();

    Set<RhP2pIp> getRhP2pIps();
}

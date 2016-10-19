package io.subutai.common.environment;


import java.util.Set;


public interface EnvironmentPeer
{
    String getPeerId();

    Integer getVlan();

    Environment getEnvironment();

    void setEnvironment( Environment environment );

    public void addRhP2pIps( Set<RhP2pIp> rhP2pIps );

    public Set<RhP2pIp> getRhP2pIps();
}

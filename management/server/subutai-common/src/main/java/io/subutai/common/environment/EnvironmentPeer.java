package io.subutai.common.environment;


public interface EnvironmentPeer
{
    String getIp();

    void setIp( String ip );

    String getPeerId();

    void setPeerId( String peerId );

    void setEnvironment( Environment environment );
}

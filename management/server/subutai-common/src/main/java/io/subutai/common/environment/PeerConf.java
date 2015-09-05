package io.subutai.common.environment;


import io.subutai.common.protocol.N2NConfig;


public interface PeerConf
{
    //    String getIp();
    //
    //    void setIp( String ip );
    //
    //    String getPeerId();
    //
    //    void setPeerId( String peerId );

    void setEnvironment( Environment environment );

    N2NConfig getN2NConfig();

    void setN2NConfig( N2NConfig config );
}

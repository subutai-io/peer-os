package io.subutai.core.metric.api.pojo;


import java.util.List;


public interface P2Pinfo
{
    String getRhId();


    void setRhId( final String rhId );


    String getRhVersion();


    void setRhVersion( final String rhVersion );


    int getP2pStatus();


    void setP2pStatus( final int p2pStatus );


    List<String> getState();


    void setState( final List<String> state );


    List<String> getP2pErrorLogs();


    void setP2pErrorLogs( final List<String> p2pErrorLogs );


    String getP2pVersion();


    void setP2pVersion( final String p2pVersion );


    int getRhVersionCheck();


    void setRhVersionCheck( int rhVersionCheck );


    int getP2pVersionCheck();


    void setP2pVersionCheck( int p2pVersionCheck );


    void setP2pVersionCheck( String from, String to );


    void setRhVersionCheck( String from, String to );
}

package io.subutai.core.metric.api.pojo;


import java.util.List;


public interface P2Pinfo
{
    String getRhId();


    String getRhVersion();


    int getP2pStatus();


    List<String> getState();


    List<String> getP2pErrorLogs();

    List<String> getP2pSystemLogs();


    String getP2pVersion();


    int getRhVersionCheck();


    int getP2pVersionCheck();

    String getRhName();
}

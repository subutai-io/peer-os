package io.subutai.core.metric.api.pojo;


import java.util.List;


public interface P2Pinfo
{
    String getRhId();


    String getRhVersion();


    int getP2pStatus();


    List<String> getState();


    String getP2pVersion();
}

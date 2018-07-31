package io.subutai.core.metric.api.pojo;


import java.util.List;


public interface P2PInfo
{
    String getRhId();


    int getP2pStatus();


    List<String> getState();
}

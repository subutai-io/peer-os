package io.subutai.core.metric.rest.ui.pojo;


import java.util.List;

import io.subutai.core.metric.api.pojo.P2PInfo;


public class P2PInfoPojo
{
    private List<P2PInfo> p2pList;


    public List<P2PInfo> getP2pList()
    {
        return p2pList;
    }


    public void setP2pList( final List<P2PInfo> p2pList )
    {
        this.p2pList = p2pList;
    }
}

package io.subutai.core.metric.rest.ui.pojo;


import java.util.List;

import io.subutai.core.metric.api.pojo.P2Pinfo;


public class P2PInfoPojo
{
    private List<P2Pinfo> p2pList;


    public List<P2Pinfo> getP2pList()
    {
        return p2pList;
    }


    public void setP2pList( final List<P2Pinfo> p2pList )
    {
        this.p2pList = p2pList;
    }
}

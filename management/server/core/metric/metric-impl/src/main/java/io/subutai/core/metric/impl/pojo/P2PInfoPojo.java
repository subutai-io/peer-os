package io.subutai.core.metric.impl.pojo;


import java.util.List;

import io.subutai.core.metric.api.pojo.P2PInfo;


public class P2PInfoPojo implements P2PInfo
{
    private String rhId;
    private int p2pStatus;
    private List<String> state;


    @Override
    public String getRhId()
    {
        return rhId;
    }


    public void setRhId( final String rhId )
    {
        this.rhId = rhId;
    }


    @Override
    public int getP2pStatus()
    {
        return p2pStatus;
    }


    public void setP2pStatus( final int p2pStatus )
    {
        this.p2pStatus = p2pStatus;
    }


    @Override
    public List<String> getState()
    {
        return state;
    }


    public void setState( final List<String> state )
    {
        this.state = state;
    }
}

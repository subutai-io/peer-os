package io.subutai.core.metric.impl.pojo;


import java.util.List;

import io.subutai.core.metric.api.pojo.P2Pinfo;


public class P2PInfoPojo implements P2Pinfo
{
    private String rhId;
    private String rhVersion;
    private String p2pVersion;
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
    public String getRhVersion()
    {
        return rhVersion;
    }


    public void setRhVersion( final String rhVersion )
    {
        this.rhVersion = rhVersion;
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
    public String getP2pVersion()
    {
        return p2pVersion;
    }


    public void setP2pVersion( final String p2pVersion )
    {
        this.p2pVersion = p2pVersion;
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

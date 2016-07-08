package io.subutai.core.metric.impl.pojo;


import io.subutai.core.metric.api.pojo.P2Pinfo;


public class P2PInfoPojo implements P2Pinfo
{
    private String rhId;
    private String rhVersion;
    private String p2pVersion;
    private int p2pStatus;
    private String state;
    private String p2pErrorLogs;


    public String getRhId()
    {
        return rhId;
    }


    public void setRhId( final String rhId )
    {
        this.rhId = rhId;
    }


    public String getRhVersion()
    {
        return rhVersion;
    }


    public void setRhVersion( final String rhVersion )
    {
        this.rhVersion = rhVersion;
    }


    public int getP2pStatus()
    {
        return p2pStatus;
    }


    public void setP2pStatus( final int p2pStatus )
    {
        this.p2pStatus = p2pStatus;
    }


    public String getP2pErrorLogs()
    {
        return p2pErrorLogs;
    }


    public void setP2pErrorLogs( final String p2pErrorLogs )
    {
        this.p2pErrorLogs = p2pErrorLogs;
    }


    public String getState()
    {
        return state;
    }


    public void setState( final String state )
    {
        this.state = state;
    }


    public String getP2pVersion()
    {
        return p2pVersion;
    }


    public void setP2pVersion( final String p2pVersion )
    {
        this.p2pVersion = p2pVersion;
    }
}

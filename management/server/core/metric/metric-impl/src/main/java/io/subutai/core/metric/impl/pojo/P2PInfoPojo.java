package io.subutai.core.metric.impl.pojo;


import java.util.List;

import io.subutai.core.metric.api.pojo.P2Pinfo;


public class P2PInfoPojo implements P2Pinfo
{
    private String rhId;
    private String rhVersion;
    private String p2pVersion;
    private int p2pStatus;
    private int p2pVersionCheck;
    private int rhVersionCheck;
    private List<String> state;
    private List<String> p2pErrorLogs;


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


    public String getP2pVersion()
    {
        return p2pVersion;
    }


    public void setP2pVersion( final String p2pVersion )
    {
        this.p2pVersion = p2pVersion;
    }

    @Override
    public int getRhVersionCheck() {
        return rhVersionCheck;
    }

    @Override
    public void setRhVersionCheck( int rhVersionCheck ) {
        this.rhVersionCheck = rhVersionCheck;
    }

    @Override
    public int getP2pVersionCheck() {
        return p2pVersionCheck;
    }

    @Override
    public void setP2pVersionCheck( int p2pVersionCheck ) {
        this.p2pVersionCheck = p2pVersionCheck;
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


    @Override
    public List<String> getP2pErrorLogs()
    {
        return p2pErrorLogs;
    }


    public void setP2pErrorLogs( final List<String> p2pErrorLogs )
    {
        this.p2pErrorLogs = p2pErrorLogs;
    }
}

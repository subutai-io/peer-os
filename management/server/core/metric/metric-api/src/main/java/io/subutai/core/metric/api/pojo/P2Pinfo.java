package io.subutai.core.metric.api.pojo;


import java.util.List;


public interface P2Pinfo
{
    public String getRhId();


    public void setRhId( final String rhId );


    public String getRhVersion();


    public void setRhVersion( final String rhVersion );


    public int getP2pStatus();


    public void setP2pStatus( final int p2pStatus );


    public List<String> getState();


    public void setState( final List<String> state );


    public List<String> getP2pErrorLogs();


    public void setP2pErrorLogs( final List<String> p2pErrorLogs );


    public String getP2pVersion();


    public void setP2pVersion( final String p2pVersion );
}

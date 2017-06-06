package io.subutai.hub.share.dto.domain;


public class P2PInfoDto
{
    public enum State
    {
        READY, FAILED, DESTROY, WAIT, CREATE, UPDATE
    }


    private String subutaiId;
    private String containerId;
    private String rhId;
    private String intefaceName;
    private String p2pIp;
    private State state;
    private String logs;


    public P2PInfoDto()
    {

    }


    public P2PInfoDto( final String subutaiId, final String rhId, final String intefaceName, final String p2pIp,
                       final State state )
    {
        this.subutaiId = subutaiId;
        this.rhId = rhId;
        this.intefaceName = intefaceName;
        this.p2pIp = p2pIp;
        this.state = state;
    }


    public String getSubutaiId()
    {
        return subutaiId;
    }


    public void setSubutaiId( final String subutaiId )
    {
        this.subutaiId = subutaiId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public String getRhId()
    {
        return rhId;
    }


    public void setRhId( final String rhId )
    {
        this.rhId = rhId;
    }


    public String getIntefaceName()
    {
        return intefaceName;
    }


    public void setIntefaceName( final String intefaceName )
    {
        this.intefaceName = intefaceName;
    }


    public String getP2pIp()
    {
        return p2pIp;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public void setP2pIp( final String p2pIp )
    {
        this.p2pIp = p2pIp;
    }


    public String getLogs()
    {
        return logs;
    }


    public void setLogs( final String logs )
    {
        this.logs = logs;
    }
}

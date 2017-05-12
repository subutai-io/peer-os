package io.subutai.hub.share.dto.domain;


public class P2PInfoDto
{
    public enum State
    {
        COLLECT_P2P_SUBNETS, SETUP_TUNNEL, SETUP_PORT_MAP, READY, FAILED, DESTROY, WAIT
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

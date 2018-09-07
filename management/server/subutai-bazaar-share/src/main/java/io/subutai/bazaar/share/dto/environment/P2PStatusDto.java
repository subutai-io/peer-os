package io.subutai.bazaar.share.dto.environment;


public class P2PStatusDto
{
    public enum State
    {
        INITIALIZING, WAITING_IP, REQUESTING_PROXIES, WAITING_PROXIES, WAITING_CONNECTION, INITIALIZING_CONNECTION,
        CONNECTED, DISCONNECTED, STOPPED, COOLDOWN, UNKNOWN
    }


    private String ip;
    private State state;
    private String lastError;


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public String getLastError()
    {
        return lastError;
    }


    public void setLastError( final String lastError )
    {
        this.lastError = lastError;
    }
}

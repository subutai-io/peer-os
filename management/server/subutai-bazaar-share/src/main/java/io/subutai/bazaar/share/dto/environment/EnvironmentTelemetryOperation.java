package io.subutai.bazaar.share.dto.environment;


public class EnvironmentTelemetryOperation
{
    public enum State
    {
        FAILED, SUCCESS
    }


    private String containerId;
    private String targetHost;
    private String command;
    private State state;
    private int timeout;
    private String logs;


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public String getTargetHost()
    {
        return targetHost;
    }


    public void setTargetHost( final String targetHost )
    {
        this.targetHost = targetHost;
    }


    public String getCommand()
    {
        return command;
    }


    public void setCommand( final String command )
    {
        this.command = command;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public int getTimeout()
    {
        return timeout;
    }


    public void setTimeout( final int timeout )
    {
        this.timeout = timeout;
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

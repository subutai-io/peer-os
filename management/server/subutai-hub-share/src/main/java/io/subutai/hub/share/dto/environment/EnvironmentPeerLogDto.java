package io.subutai.hub.share.dto.environment;


public class EnvironmentPeerLogDto
{
    public enum LogType
    {
        ERROR,
        INFO,
        DEBUG
    }


    public enum LogEvent
    {
        NETWORK,
        CONTAINER,
        REQUEST_TO_HUB,
        SUBUTAI,
        HUB
    }


    private String peerId;
    private EnvironmentPeerDto.PeerState peerState;
    private LogType logType;
    private LogEvent logEvent;
    private Integer logCode;
    private String envId;
    private String message;
    private String exceptionMessage;
    private String containerId;


    public EnvironmentPeerLogDto()
    {
    }


    public EnvironmentPeerLogDto( final String peerId, final EnvironmentPeerDto.PeerState peerState,
                                  final LogType logType, final Integer logCode, final String envId,
                                  final String message, final String exceptionMessage, final String containerId )
    {
        this.peerId = peerId;
        this.peerState = peerState;
        this.logType = logType;
        this.logCode = logCode;
        this.envId = envId;
        this.message = message;
        this.exceptionMessage = exceptionMessage;
        this.containerId = containerId;
    }


    public EnvironmentPeerLogDto( final String peerId, final EnvironmentPeerDto.PeerState peerState, final String envId,
                                  final LogType logType )
    {
        this.peerId = peerId;
        this.peerState = peerState;
        this.envId = envId;
        this.logType = logType;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public LogType getLogType()
    {
        return logType;
    }


    public void setLogType( final LogType logType )
    {
        this.logType = logType;
    }


    public String getEnvId()
    {
        return envId;
    }


    public void setEnvId( final String envId )
    {
        this.envId = envId;
    }


    public String getMessage()
    {
        return message;
    }


    public void setMessage( final String message )
    {
        this.message = message;
    }


    public EnvironmentPeerDto.PeerState getPeerState()
    {
        return peerState;
    }


    public void setPeerState( final EnvironmentPeerDto.PeerState peerState )
    {
        this.peerState = peerState;
    }


    public Integer getLogCode()
    {
        return logCode;
    }


    public void setLogCode( final Integer logCode )
    {
        this.logCode = logCode;
    }


    public String getExceptionMessage()
    {
        return exceptionMessage;
    }


    public void setExceptionMessage( final String exceptionMessage )
    {
        this.exceptionMessage = exceptionMessage;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public LogEvent getLogEvent()
    {
        return logEvent;
    }


    public void setLogEvent( final LogEvent logEvent )
    {
        this.logEvent = logEvent;
    }
}

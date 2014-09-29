package org.safehaus.subutai.common.protocol;


import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;


/**
 * Created by timur on 9/20/14.
 */
public abstract class PeerCommandMessage
{
    protected UUID id;
    protected UUID agentId;
    protected UUID peerId;
    protected UUID envId;
    protected PeerCommandType type = PeerCommandType.UNKNOWN;
    protected String exceptionMessage;
    protected boolean success = false;
    protected boolean proccessed = false;


    private PeerCommandMessage()
    {
    }


    public PeerCommandMessage( PeerCommandType type, UUID envId, UUID peerId, UUID agentId )
    {
        this.id = UUID.randomUUID();
        this.peerId = peerId;
        this.agentId = agentId;
        this.envId = envId;
        this.type = type;
    }


    public UUID getAgentId()
    {
        return agentId;
    }


    public void setAgentId( final UUID agentId )
    {
        this.agentId = agentId;
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final UUID peerId )
    {
        this.peerId = peerId;
    }


    public PeerCommandType getType()
    {
        return type;
    }


    public void setType( final PeerCommandType type )
    {
        this.type = type;
    }


    public String getExceptionMessage()
    {
        return exceptionMessage;
    }


    public void setExceptionMessage( final String exceptionMessage )
    {
        this.exceptionMessage = exceptionMessage;
    }


    public String toJson()
    {
        return JsonUtil.toJson( this );
    }


    public boolean isSuccess()
    {
        return success;
    }


    public void setSuccess( final boolean success )
    {
        this.success = success;
    }


    public boolean isProccessed()
    {
        return proccessed;
    }


    public void setProccessed( final boolean proccessed )
    {
        this.proccessed = proccessed;
    }


    abstract public void setResult( Object result );

    abstract public Object getResult();


    public UUID getId()
    {
        return id;
    }


    public UUID getEnvId()
    {
        return envId;
    }


    public void setEnvId( final UUID envId )
    {
        this.envId = envId;
    }


    @Override
    public String toString()
    {
        return "PeerCommandMessage{" +
                "agentId=" + agentId +
                ", peerId=" + peerId +
                ", type=" + type +
                ", exceptionMessage=" + exceptionMessage +
                ", success=" + success +
                '}';
    }
}

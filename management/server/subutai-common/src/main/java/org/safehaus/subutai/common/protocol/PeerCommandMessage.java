package org.safehaus.subutai.common.protocol;


import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;


/**
 * Created by timur on 9/20/14.
 */
public abstract class PeerCommandMessage
{
    protected UUID agentId;
    protected UUID peerId;
    protected PeerCommandType type;
    protected String exceptionMessage;
    protected boolean success = false;


    public PeerCommandMessage()
    {
        this.type = PeerCommandType.UNKNOWN;
    }


    public PeerCommandMessage( PeerCommandType type, UUID peerId, UUID agentId )
    {
        this.peerId = peerId;
        this.agentId = agentId;
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


    abstract public void setResult(Object result);

    abstract public Object getResult();

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

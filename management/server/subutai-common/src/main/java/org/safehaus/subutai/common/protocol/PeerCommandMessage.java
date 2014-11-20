package org.safehaus.subutai.common.protocol;


import java.lang.reflect.Type;
import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;


/**
 * Created by timur on 9/20/14.
 */
public abstract class PeerCommandMessage
{
    //TODO: implement me
    protected UUID sourcePeerId;
    protected UUID id;
    protected UUID agentId;
    protected UUID peerId;
    //    protected UUID envId;
    protected PeerCommandType type = PeerCommandType.UNKNOWN;
    protected String exceptionMessage;
    protected boolean success = false;
    protected boolean proccessed = false;
    protected String jsonResult;
    protected String jsonInput;
    protected long createTimestamp = System.currentTimeMillis();
    protected long completeTimestamp;


    public void setCompleteTimestamp( final long completeTimestamp )
    {
        this.completeTimestamp = completeTimestamp;
    }


    public long getCreateTimestamp()
    {
        return createTimestamp;
    }


    public long getCompleteTimestamp()
    {
        return completeTimestamp;
    }


    private PeerCommandMessage()
    {
    }


    public PeerCommandMessage( PeerCommandType type,/* UUID envId,*/ UUID peerId, UUID agentId )
    {
        this.id = UUIDUtil.generateTimeBasedUUID();
        this.peerId = peerId;
        this.agentId = agentId;
        //        this.envId = envId;
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
        this.success = false;
    }


    public String toJson()
    {
        return JsonUtil.toJson( this );
    }


    public boolean isSuccess()
    {
        return success;
    }
    //
    //
    //    public void setSuccess( final boolean success )
    //    {
    //        this.success = success;
    //    }


    public boolean isProccessed()
    {
        return proccessed;
    }


    public void setProccessed( final boolean proccessed )
    {
        this.proccessed = proccessed;
    }


    public void setResult( Object result )
    {
        this.jsonResult = JsonUtil.toJson( result, getResultObjectType() );
        this.success = true;
    }


    public UUID getId()
    {
        return id;
    }


    //    public UUID getEnvironmentId()
    //    {
    //        return envId;
    //    }
    //
    //
    //    public void setEnvironmentId( final UUID envId )
    //    {
    //        this.envId = envId;
    //    }
    //


    public Object getResult()
    {
        return JsonUtil.fromJson( this.jsonResult, getResultObjectType() );
    }


    public Object getInput()
    {
        return JsonUtil.fromJson( this.jsonInput, getInputObjectType() );
    }


    public void setInput( final String input )
    {
        this.jsonInput = JsonUtil.toJson( input, getInputObjectType() );
    }


    abstract public Type getResultObjectType();

    abstract public Type getInputObjectType();


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

package org.safehaus.agent.command.model;


import java.util.UUID;


/**
 * A minimal abstract Message class.
 */
public abstract class AbstractMessage implements Message
{
    public static final long SEQ_NUM_NOT_SET = -1;

    private UUID messageId;
    private UUID agentId;
    private UUID issuerId;


    protected AbstractMessage( UUID issuerId, UUID agentId, UUID messageId )
    {
        this.issuerId = issuerId;
        this.agentId = agentId;
        this.messageId = messageId;
    }


    @Override
    public abstract Type getType();


    @Override
    public UUID getMessageId()
    {
        return messageId;
    }


    @Override
    public UUID getAgentId()
    {
        return agentId;
    }


    @Override
    public UUID getIssuerId()
    {
        return issuerId;
    }
}

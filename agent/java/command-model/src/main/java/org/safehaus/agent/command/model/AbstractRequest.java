package org.safehaus.agent.command.model;


import java.util.UUID;


/**
 * An abstract Message for requests.
 */
public abstract class AbstractRequest extends AbstractMessage implements Request
{
    private long requestSeqNum = SEQ_NUM_NOT_SET;


    protected AbstractRequest( long requestSeqNum, UUID issuerId, UUID agentId, UUID messageId )
    {
        super( issuerId, agentId, messageId );

        this.requestSeqNum = requestSeqNum;
    }


    @Override
    public final boolean isRequest()
    {
        return true;
    }


    @Override
    public final boolean isResponse()
    {
        return false;
    }


    @Override
    public long getRequestSeqNum()
    {
        return requestSeqNum;
    }
}

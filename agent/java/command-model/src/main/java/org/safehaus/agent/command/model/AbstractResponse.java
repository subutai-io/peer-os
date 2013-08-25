package org.safehaus.agent.command.model;


import java.util.UUID;


/**
 * An abstract Message for responses.
 */
public abstract class AbstractResponse<T extends Request> extends AbstractMessage implements Response
{
    private long responseSeqNum = SEQ_NUM_NOT_SET;

    private final T request;


    protected AbstractResponse( T request, long responseSeqNum, UUID issuerId, UUID agentId, UUID messageId )
    {
        super( issuerId, agentId, messageId );

        this.request = request;
        this.responseSeqNum = responseSeqNum;
    }


    @Override
    public final boolean isRequest()
    {
        return false;
    }


    @Override
    public final boolean isResponse()
    {
        return true;
    }


    @Override
    public long getResponseSeqNum()
    {
        return responseSeqNum;
    }


    @Override
    public T getRequest()
    {
        return request;
    }
}

package org.safehaus.agent.command.model;


import java.util.UUID;


/**
 * A Response interface linked to the triggering Request.
 */
public interface Response<T extends Request>
{
    /**
     * Gets the response sequence number associated with this Message. Some
     * Messages will be requests with a 0 sequence number, and will have
     * several response Messages sent in response to the request. The sequence
     * number of the first response for example will be 1.
     *
     * @return the response sequence number for this Message
     */
    long getResponseSeqNum();

    /**
     * The Request that resulted in this response.
     *
     * @return the Request triggering this Response
     */
    T getRequest();
}

package org.safehaus.agent.command.model;

// TODO - Add Apache Header here

import java.util.UUID;


/**
 * The interface for Kiskis Agent commands.
 */
public interface Message
{
    /**
     * Gets the type of this Message.
     *
     * @return this Message's type
     */
    Type getType();

    /**
     * Gets the UUID associated with this Message.
     *
     * @return the uuid of this Message
     */
    UUID getMessageId();

    /**
     * The UUID of the Agent this Message is being sent to.
     *
     * @return the agent UUID for this Message
     */
    UUID getAgentId();

    /**
     * The UUID of the issuer of this Message: basically what created and sent
     * this message.
     *
     * @return the issuer UUID for this Message
     */
    UUID getIssuerId();

    /**
     * Gets whether or not this Message is a request.
     *
     * @return true if a request, false if a response
     */
    boolean isRequest();

    /**
     * Gets whether or not this Message is a response.
     *
     * @return true if a response, false if a request
     */
    boolean isResponse();
}

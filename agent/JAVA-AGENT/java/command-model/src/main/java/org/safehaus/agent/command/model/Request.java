package org.safehaus.agent.command.model;


/**
 * Created with IntelliJ IDEA.
 * User: akarasulu
 * Date: 8/25/13
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Request
{
    /**
     * Gets the request sequence number associated with this Message. This is
     * a unique ordered sequence number for each request issues for an agent
     * since the beginning of operation. So if 20 request commands are executed
     * the next request will have a sequence number of 21. This sequence number
     * is used to optionally require certain commands to be executed and
     * completed before others are allowed to execute when for example the
     * order of execution matters. This sequence number is distinct from the
     * response sequence number, which is used to order responses to requests.
     *
     * @return the request sequence number for this Message
     */
    long getRequestSeqNum();
}

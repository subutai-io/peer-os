package io.subutai.common.command;


import java.util.Set;
import java.util.UUID;


/**
 * Response from agent
 */
public interface Response
{
    /**
     * Represents command response type executed by agent
     *
     * @return - Response type
     */
    public ResponseType getType();

    /**
     * Target Host id from where response is coming from
     *
     * @return - unique id of host
     */
    public String getId();

    /**
     * Command Id saved in database, will be needed to update specific command status.
     *
     * @return - unique id of command
     */
    public UUID getCommandId();

    /**
     * ID of a process executed this command
     *
     * @return - process id
     */
    public Integer getPid();

    /**
     * Command output chunks, it is possible to track the sequence of command output
     *
     * @return - response count
     */
    public Integer getResponseNumber();

    /**
     * Command output
     *
     * @return - command output string
     */
    public String getStdOut();


    /**
     * Error message if any occurred while executing command
     *
     * @return - error string message
     */
    public String getStdErr();


    /**
     * Command exit code
     *
     * @return - exit code for a command
     */
    public Integer getExitCode();

    public Set<String> getConfigPoints();
}

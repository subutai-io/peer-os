package org.safehaus.subutai.common.command;


/**
 * Command Result returned by Host.execute
 */
public interface CommandResult
{

    /**
     * Command execution result
     * @return - command exit code
     */
    public Integer getExitCode();


    /**
     * Prints command output
     * @return - appropriate output result for a command
     */
    public String getStdOut();


    /**
     * Prints error messages if any
     * @return - error messages for a command
     */
    public String getStdErr();


    /**
     * Command exit code status
     * @return - true if status equals CommandStatus.SUCCEEDED
     */
    public boolean hasSucceeded();


    /**
     * Notification if command is still running or not
     * @return - status of command
     */
    public boolean hasCompleted();

    /**
     * Represents if process executing command was killed or reached its timeout
     * @return - command status if it reached its timeout
     */
    public boolean hasTimedOut();


    /**
     * Current status of a command
     * @return - Returns status of a command
     */
    public CommandStatus getStatus();
}

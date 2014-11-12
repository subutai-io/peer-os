package org.safehaus.subutai.common.command;


/**
 * Command Result returned by Host.execute
 */
public interface CommandResult
{

    public Integer getExitCode();


    public String getStdOut();


    public String getStdErr();


    public boolean hasSucceeded();


    public boolean hasCompleted();


    public boolean hasTimedOut();


    public CommandStatus getStatus();
}

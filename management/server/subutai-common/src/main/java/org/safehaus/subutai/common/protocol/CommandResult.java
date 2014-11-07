package org.safehaus.subutai.common.protocol;


import java.util.UUID;


/**
 * Command Result returned by Host.execute
 */
public class CommandResult
{
    private final UUID commandId;
    private final Integer exitCode;
    private final String stdOut;
    private final String stdErr;
    private final CommandStatus status;


    public CommandResult( final UUID commandId, final Integer exitCode, final String stdOut, final String stdErr,
                          CommandStatus status )
    {
        this.commandId = commandId;
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.status = status;
    }


    public UUID getCommandId()
    {
        return commandId;
    }


    public Integer getExitCode()
    {
        return exitCode;
    }


    public String getStdOut()
    {
        return stdOut;
    }


    public String getStdErr()
    {
        return stdErr;
    }


    public boolean hasSucceeded()
    {
        return status == CommandStatus.SUCCEEDED;
    }


    public boolean hasCompleted()
    {
        return status == CommandStatus.FAILED || status == CommandStatus.SUCCEEDED;
    }


    public boolean hasTimedOut()
    {
        return status == CommandStatus.TIMEOUT;
    }


    public CommandStatus getStatus()
    {
        return status;
    }
}

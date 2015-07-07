package io.subutai.core.executor.impl;


import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;

import com.google.common.base.Objects;


/**
 * Command Result returned by Host.execute
 */
public class CommandResultImpl implements CommandResult
{
    private final Integer exitCode;
    private final String stdOut;
    private final String stdErr;
    private final CommandStatus status;


    public CommandResultImpl( final Integer exitCode, final String stdOut, final String stdErr, CommandStatus status )
    {
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.status = status;
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
        return status == CommandStatus.TIMEOUT || status == CommandStatus.KILLED;
    }


    public CommandStatus getStatus()
    {
        return status;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "exitCode", exitCode ).add( "stdOut", stdOut )
                      .add( "stdErr", stdErr ).add( "status", status ).toString();
    }
}

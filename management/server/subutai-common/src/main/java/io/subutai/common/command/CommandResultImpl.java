package io.subutai.common.command;


import com.google.common.base.MoreObjects;


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


    public CommandResultImpl( final CommandResult commandResult )
    {
        this.exitCode = commandResult.getExitCode();
        this.stdOut = commandResult.getStdOut();
        this.stdErr = commandResult.getStdErr();
        this.status = commandResult.getStatus();
    }


    @Override
    public Integer getExitCode()
    {
        return exitCode;
    }


    @Override
    public String getStdOut()
    {
        return stdOut;
    }


    @Override
    public String getStdErr()
    {
        return stdErr;
    }


    @Override
    public boolean hasSucceeded()
    {
        return status == CommandStatus.SUCCEEDED;
    }


    @Override
    public boolean hasCompleted()
    {
        return status == CommandStatus.FAILED || status == CommandStatus.SUCCEEDED;
    }


    @Override
    public boolean hasTimedOut()
    {
        return status == CommandStatus.TIMEOUT || status == CommandStatus.KILLED;
    }


    @Override
    public CommandStatus getStatus()
    {
        return status;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "exitCode", exitCode ).add( "stdOut", stdOut )
                          .add( "stdErr", stdErr ).add( "status", status ).toString();
    }
}

package io.subutai.common.command;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;


/**
 * Command Result returned by Host.execute
 */
public class CommandResultImpl implements CommandResult
{
    @JsonProperty( value = "exitCode" )
    private final Integer exitCode;

    @JsonProperty( value = "stdOut" )
    private final String stdOut;

    @JsonProperty( value = "stdErr" )
    private final String stdErr;

    @JsonProperty( value = "status" )
    private final CommandStatus status;


    public CommandResultImpl( @JsonProperty( value = "exitCode" ) final Integer exitCode,
                              @JsonProperty( value = "stdOut" ) final String stdOut,
                              @JsonProperty( value = "stdErr" ) final String stdErr,
                              @JsonProperty( value = "status" ) CommandStatus status )
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

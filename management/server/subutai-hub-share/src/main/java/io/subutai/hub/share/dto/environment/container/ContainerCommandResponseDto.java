package io.subutai.hub.share.dto.environment.container;


import com.google.common.base.MoreObjects;


public class ContainerCommandResponseDto
{

    // id of target container
    private final String containerId;

    // id of corresponding command request
    private final String commandId;

    // can be null if command timed out, 0 indicates successful completion of command,
    // no-zero indicates  failure
    private Integer exitCode;

    // standard output of command
    private String stdOut;

    // error output of command
    private String stdErr;

    // flag indicating if command has timed out or completed
    private boolean hasTimedOut;

    //holds exception message if any exception occurs during command execution
    private String exception;


    public ContainerCommandResponseDto( final String containerId, final String commandId, final Integer exitCode,
                                        final String stdOut, final String stdErr, final boolean hasTimedOut )
    {
        this.containerId = containerId;
        this.commandId = commandId;
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.hasTimedOut = hasTimedOut;
    }


    public ContainerCommandResponseDto( final String containerId, final String commandId, final String exception )
    {
        this.containerId = containerId;
        this.commandId = commandId;
        this.exception = exception;
    }


    public String getCommandId()
    {
        return commandId;
    }


    public String getException()
    {
        return exception;
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


    public boolean hasTimedOut()
    {
        return hasTimedOut;
    }


    public String getContainerId()
    {
        return containerId;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "containerId", containerId ).add( "commandId", commandId )
                          .add( "exitCode", exitCode ).add( "stdOut", stdOut ).add( "stdErr", stdErr )
                          .add( "hasTimedOut", hasTimedOut ).add( "exception", exception ).toString();
    }
}

package io.subutai.hub.share.dto.environment.container;


public class ContainerCommandResponseDto
{

    // id of target container
    private String containerId;

    // id of corresponding command request
    private String commandId;

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


    protected ContainerCommandResponseDto()
    {
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
        final StringBuffer sb = new StringBuffer( "ContainerCommandResponseDto{" );
        sb.append( "containerId='" ).append( containerId ).append( '\'' );
        sb.append( ", commandId='" ).append( commandId ).append( '\'' );
        sb.append( ", exitCode=" ).append( exitCode );
        sb.append( ", stdOut='" ).append( stdOut ).append( '\'' );
        sb.append( ", stdErr='" ).append( stdErr ).append( '\'' );
        sb.append( ", hasTimedOut=" ).append( hasTimedOut );
        sb.append( ", exception='" ).append( exception ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}

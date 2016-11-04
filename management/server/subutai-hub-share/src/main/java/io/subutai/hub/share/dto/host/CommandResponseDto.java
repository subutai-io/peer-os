package io.subutai.hub.share.dto.host;


public class CommandResponseDto
{

    // id of target host
    private String hostId;

    // id of corresponding command request
    private String commandId;

    // can be null if command timed out, 0 indicates successful completion of command,
    // non-zero indicates  failure
    private Integer exitCode;

    // standard output of command
    private String stdOut;

    // error output of command
    private String stdErr;

    // flag indicating if command has timed out or completed
    private boolean hasTimedOut;

    //holds exception message if any exception occurs during command execution
    private String exception;


    public CommandResponseDto( final String hostId, final String commandId, final Integer exitCode, final String stdOut,
                               final String stdErr, final boolean hasTimedOut )
    {
        this.hostId = hostId;
        this.commandId = commandId;
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.hasTimedOut = hasTimedOut;
    }


    protected CommandResponseDto()
    {
    }


    public CommandResponseDto( final String hostId, final String commandId, final String exception )
    {
        this.hostId = hostId;
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


    public String getHostId()
    {
        return hostId;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "CommandResponseDto{" );
        sb.append( "hostId='" ).append( hostId ).append( '\'' );
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

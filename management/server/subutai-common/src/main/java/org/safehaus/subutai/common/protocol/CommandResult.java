package org.safehaus.subutai.common.protocol;


/**
 * Command Result returned by Host.execute
 */
public class CommandResult
{
    private final Integer exitCode;
    private final String stdOut;
    private final String stdErr;


    public CommandResult( final Integer exitCode, final String stdOut, final String stdErr )
    {
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
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
        return exitCode == 0;
    }


    public boolean hasCompleted()
    {
        return exitCode != null;
    }


    public boolean hasTimedOut()
    {
        return exitCode == null;
    }
}

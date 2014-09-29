package org.safehaus.subutai.common.protocol;


import java.util.UUID;

import org.safehaus.subutai.common.enums.RequestType;


/**
 * Created by timur on 9/25/14.
 */
public class ExecuteCommandMessage extends PeerCommandMessage
{
    private Integer exitCode;
    private String stdOut;
    private String stdErr;
    private String command;
    private RequestType requestType;
    private int timeout;
    private String cwd;


    public ExecuteCommandMessage( UUID envId, UUID peerId, UUID agentId, String command, RequestType requestType,
                                  int timeout, String cwd )
    {
        super( PeerCommandType.EXECUTE, envId, peerId, agentId );
        this.command = command;
        this.requestType = requestType;
        this.timeout = timeout;
        this.cwd = cwd;
    }


    public Integer getExitCode()
    {
        return exitCode;
    }


    public void setExitCode( final Integer exitCode )
    {
        this.exitCode = exitCode;
    }


    public String getStdOut()
    {
        return stdOut;
    }


    public void setStdOut( final String stdOut )
    {
        this.stdOut = stdOut;
    }


    public String getStdErr()
    {
        return stdErr;
    }


    public void setStdErr( final String stdErr )
    {
        this.stdErr = stdErr;
    }


    @Override
    public void setResult( final Object result )
    {

    }


    @Override
    public Object getResult()
    {
        return null;
    }


    public String getCommand()
    {
        return command;
    }


    public RequestType getRequestType()
    {
        return requestType;
    }


    public String getCwd()
    {
        return cwd;
    }


    public int getTimeout()
    {
        return timeout;
    }
}

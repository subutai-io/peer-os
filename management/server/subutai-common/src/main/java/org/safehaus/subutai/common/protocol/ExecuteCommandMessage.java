package org.safehaus.subutai.common.protocol;


import java.lang.reflect.Type;
import java.util.UUID;

import org.safehaus.subutai.common.enums.RequestType;

import com.google.gson.reflect.TypeToken;


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
    private long timeout;
    private String cwd;


    public ExecuteCommandMessage( UUID envId, UUID peerId, UUID agentId, String command, RequestType requestType,
                                  long timeout, String cwd )
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


    public long getTimeout()
    {
        return timeout;
    }


    @Override
    public Type getResultObjectType()
    {
        return new TypeToken<ExecutionResult>()
        {
        }.getType();
    }


    public ExecutionResult createExecutionResult( final String stdOut, final String stdErr, final Integer exitCode )
    {
        return new ExecutionResult( stdOut, stdErr, exitCode );
    }


    public class ExecutionResult
    {
        private String stdOut;
        private String stdErr;
        private int exitCode;


        public ExecutionResult( String stdOut, String stdErr, int exitCode )
        {
            this.stdOut = stdOut;
            this.stdErr = stdErr;
            this.exitCode = exitCode;
        }


        public String getStdOut()
        {
            return stdOut;
        }


        public String getStdErr()
        {
            return stdErr;
        }


        public int getExitCode()
        {
            return exitCode;
        }
    }
}

package io.subutai.common.command;


import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;


/**
 * Represents response to command from host
 */
public class ResponseImpl implements Response
{
    private ResponseType type;
    private String id;
    private UUID commandId;
    private Integer pid;
    private Integer responseNumber;
    private String stdOut;
    private String stdErr;
    private Integer exitCode;


    public ResponseImpl( final Response response )
    {
        Preconditions.checkNotNull( response );

        this.type = response.getType();
        this.id = response.getId();
        this.commandId = response.getCommandId();
        this.pid = response.getPid();
        this.responseNumber = response.getResponseNumber();
        this.stdOut = response.getStdOut();
        this.stdErr = response.getStdErr();
        this.exitCode = response.getExitCode();
    }


    @Override
    public ResponseType getType()
    {
        return type;
    }


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public UUID getCommandId()
    {
        return commandId;
    }


    @Override
    public Integer getPid()
    {
        return pid;
    }


    @Override
    public Integer getResponseNumber()
    {
        return responseNumber;
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
    public Integer getExitCode()
    {
        return exitCode;
    }


    @Override
    public String toString()
    {
        String out = StringUtils.abbreviate( stdOut, 1000 );

        return MoreObjects.toStringHelper( this ).add( "type", type ).add( "id", id ).add( "commandId", commandId )
                          .add( "pid", pid ).add( "responseNumber", responseNumber ).add( "stdOut", out )
                          .add( "stdErr", stdErr ).add( "exitCode", exitCode ).toString();
    }
}

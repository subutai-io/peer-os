package io.subutai.common.command;


import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE )
                .append( "type", type )
                .append( "id", id )
                .append( "commandId", commandId )
                .append( "pid", pid )
                .append( "responseNumber", responseNumber )
                .append( "stdOut", StringUtils.abbreviate( stdOut, 500 ) )
                .append( "stdErr", stdErr )
                .append( "exitCode", exitCode )
                .toString();
    }
}

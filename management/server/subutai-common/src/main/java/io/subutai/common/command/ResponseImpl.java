package io.subutai.common.command;


import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;


/**
 * Represents response to command from host
 */
public class ResponseImpl implements Response
{
    @JsonProperty( value = "type" )
    private ResponseType type;

    @JsonProperty( value = "id" )
    private String id;

    @JsonProperty( value = "commandId" )
    private UUID commandId;

    @JsonProperty( value = "pid" )
    private Integer pid;

    @JsonProperty( value = "responseNumber" )
    private Integer responseNumber;

    @JsonProperty( value = "stdOut" )
    private String stdOut;

    @JsonProperty( value = "stdErr" )
    private String stdErr;

    @JsonProperty( value = "exitCode" )
    private Integer exitCode;


    public ResponseImpl( @JsonProperty( value = "type" ) final ResponseType type,
                         @JsonProperty( value = "id" ) final String id,
                         @JsonProperty( value = "commandId" ) final UUID commandId,
                         @JsonProperty( value = "pid" ) final Integer pid,
                         @JsonProperty( value = "responseNumber" ) final Integer responseNumber,
                         @JsonProperty( value = "stdOut" ) final String stdOut,
                         @JsonProperty( value = "stdErr" ) final String stdErr,
                         @JsonProperty( value = "exitCode" ) final Integer exitCode )
    {
        this.type = type;
        this.id = id;
        this.commandId = commandId;
        this.pid = pid;
        this.responseNumber = responseNumber;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.exitCode = exitCode;
    }


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
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "type", type ).append( "id", id )
                                                                            .append( "commandId", commandId )
                                                                            .append( "pid", pid )
                                                                            .append( "responseNumber", responseNumber )
                                                                            .append( "stdOut", StringUtils
                                                                                    .abbreviate( stdOut, 500 ) )
                                                                            .append( "stdErr", stdErr )
                                                                            .append( "exitCode", exitCode ).toString();
    }
}

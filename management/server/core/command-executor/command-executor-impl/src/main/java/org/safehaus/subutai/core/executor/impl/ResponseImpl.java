package org.safehaus.subutai.core.executor.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.command.ResponseType;

import com.google.common.base.Objects;


/**
 * Represents response to command from host
 */
public class ResponseImpl implements Response
{
    private ResponseType type;
    private UUID id;
    private UUID commandId;
    private Integer pid;
    private Integer responseNumber;
    private String stdOut;
    private String stdErr;
    private Integer exitCode;
    private Set<String> configPoints;


    @Override
    public ResponseType getType()
    {
        return type;
    }


    @Override
    public UUID getId()
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
    public Set<String> getConfigPoints()
    {
        return configPoints;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "type", type ).add( "id", id ).add( "commandId", commandId )
                      .add( "pid", pid ).add( "responseNumber", responseNumber ).add( "stdOut", stdOut )
                      .add( "stdErr", stdErr ).add( "exitCode", exitCode ).add( "configPoints", configPoints )
                      .toString();
    }
}

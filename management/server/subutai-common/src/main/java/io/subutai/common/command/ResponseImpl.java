package io.subutai.common.command;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.command.Response;
import io.subutai.common.command.ResponseType;

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
    private Set<String> configPoints;


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
        this.configPoints = response.getConfigPoints();
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
    public Set<String> getConfigPoints()
    {
        return configPoints;
    }
}

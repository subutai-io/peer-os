package org.safehaus.subutai.core.peer.impl.command;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.command.ResponseType;


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


    public ResponseImpl( final ResponseType type, final UUID id, final UUID commandId, final Integer pid,
                         final Integer responseNumber, final String stdOut, final String stdErr, final Integer exitCode,
                         final Set<String> configPoints )
    {
        this.type = type;
        this.id = id;
        this.commandId = commandId;
        this.pid = pid;
        this.responseNumber = responseNumber;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.exitCode = exitCode;
        this.configPoints = configPoints;
    }


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
}

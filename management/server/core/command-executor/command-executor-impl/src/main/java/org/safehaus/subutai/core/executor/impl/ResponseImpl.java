package org.safehaus.subutai.core.executor.impl;


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
    private int pid;
    private int responseNumber;
    private String stdOut;
    private String stdErr;
    private int exitCode;
    private Set<String> configPoints;


    //temporary constructor for migration purposes. TODO remove after migration to new command executor
    public ResponseImpl( final ResponseType type, final UUID id, final UUID commandId, final int pid,
                         final int responseNumber, final String stdOut, final String stdErr, final int exitCode,
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
    public int getPid()
    {
        return pid;
    }


    @Override
    public int getResponseNumber()
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
    public int getExitCode()
    {
        return exitCode;
    }


    @Override
    public Set<String> getConfigPoints()
    {
        return configPoints;
    }
}

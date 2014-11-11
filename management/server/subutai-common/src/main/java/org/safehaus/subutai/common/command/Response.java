package org.safehaus.subutai.common.command;


import java.util.Set;
import java.util.UUID;


/**
 * Response from agent
 */
public interface Response
{
    public ResponseType getType();

    public UUID getId();

    public UUID getCommandId();

    public Integer getPid();

    public Integer getResponseNumber();

    public String getStdOut();

    public String getStdErr();

    public Integer getExitCode();

    public Set<String> getConfigPoints();
}

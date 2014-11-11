package org.safehaus.subutai.common.command;


import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Request to agent
 */
public interface Request
{
    public RequestType getType();

    public UUID getId();

    public UUID getCommandId();

    public String getWorkingDirectory();

    public String getCommand();

    public Set<String> getArgs();

    public Map<String, String> getEnvironment();

    public OutputRedirection getStdOut();

    public OutputRedirection getStdErr();

    public String getRunAs();

    public Integer getTimeout();
}

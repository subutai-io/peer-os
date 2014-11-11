package org.safehaus.subutai.common.command;


import java.util.List;
import java.util.Map;
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

    public List<String> getArgs();

    public Map<String, String> getEnvironment();

    public OutputRedirection getStdOut();

    public OutputRedirection getStdErr();

    public String getRunAs();

    public Integer getTimeout();

    public Integer isDaemon();
}

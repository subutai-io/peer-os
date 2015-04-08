package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;


public interface CliCommand extends Serializable
{
    public String getScope();

    public String getName();

    public String getCommand();
}

package org.safehaus.subutai.env.api;


import java.util.Set;
import java.util.UUID;


/**
 * Environment
 */
public interface Environment
{
    public UUID getId();

    public Set<EnvironmentContainer> getContainers();
}

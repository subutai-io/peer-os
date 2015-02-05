package org.safehaus.subutai.common.peer;


import java.util.Set;
import java.util.UUID;


public interface EnvironmentDestructionResult
{
    public Set<UUID> getDestroyedContainersIds();

    public EnvironmentDestructionException getException();
}

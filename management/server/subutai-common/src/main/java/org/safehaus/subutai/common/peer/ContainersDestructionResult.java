package org.safehaus.subutai.common.peer;


import java.util.Set;
import java.util.UUID;


public interface ContainersDestructionResult
{
    public Set<UUID> getDestroyedContainersIds();

    public String getException();
}

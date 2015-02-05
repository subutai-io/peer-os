package org.safehaus.subutai.common.peer;


import java.util.Set;
import java.util.UUID;


public interface ContainerDestructionResult
{
    public Set<UUID> getDestroyedContainersIds();

    public ContainerDestructionException getException();
}

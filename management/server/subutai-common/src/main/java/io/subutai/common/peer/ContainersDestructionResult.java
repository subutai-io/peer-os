package io.subutai.common.peer;


import java.util.Set;
import java.util.UUID;


public interface ContainersDestructionResult
{
    public UUID peerId();

    public Set<UUID> getDestroyedContainersIds();

    public String getException();
}

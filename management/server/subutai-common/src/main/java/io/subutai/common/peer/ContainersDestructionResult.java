package io.subutai.common.peer;


import java.util.Set;


public interface ContainersDestructionResult
{
    public String peerId();

    public Set<String> getDestroyedContainersIds();

    public String getException();
}

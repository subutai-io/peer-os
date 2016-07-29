package io.subutai.common.peer;


import java.util.Set;


public interface EnvironmentContainerHost extends ContainerHost
{
    EnvironmentContainerHost addTag( String tag );

    EnvironmentContainerHost removeTag( String tag );

    EnvironmentContainerHost setContainerName( String name );

    Set<String> getTags();

    EnvironmentContainerHost setContainerSize( ContainerSize size ) throws PeerException;
}

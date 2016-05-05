package io.subutai.common.peer;


import java.util.Set;


public interface EnvironmentContainerHost extends ContainerHost
{
    void addTag( String tag );

    void removeTag( String tag );

    Set<String> getTags();

    void setHostname( String newHostname ) throws PeerException;
}

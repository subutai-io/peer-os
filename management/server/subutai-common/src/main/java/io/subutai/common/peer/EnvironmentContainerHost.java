package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.security.SshKeys;


public interface EnvironmentContainerHost extends ContainerHost
{
    EnvironmentContainerHost addTag( String tag );

    EnvironmentContainerHost removeTag( String tag );

    Set<String> getTags();

    EnvironmentContainerHost setContainerSize( ContainerSize size ) throws PeerException;

    SshKeys getAuthorizedKeys() throws PeerException;

    Integer getDomainPort();
}

package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


public class AddSshKeyStep
{
    private final Set<String> sshKeys;
    private final EnvironmentImpl environment;
    private final NetworkManager networkManager;


    public AddSshKeyStep( final Set<String> sshKeys, final EnvironmentImpl environment,
                          final NetworkManager networkManager )
    {
        this.sshKeys = sshKeys;
        this.environment = environment;
        this.networkManager = networkManager;
    }


    public void execute() throws NetworkManagerException
    {
        Set<ContainerHost> ch = Sets.newHashSet();
        ch.addAll( environment.getContainerHosts() );

        for ( String sshKey : sshKeys )
        {
            if ( !Strings.isNullOrEmpty( sshKey ) )
            {
                environment.addSshKey( sshKey );

                //add ssh key to each environment container
                networkManager.addSshKeyToAuthorizedKeys( ch, sshKey );
            }
        }
    }
}

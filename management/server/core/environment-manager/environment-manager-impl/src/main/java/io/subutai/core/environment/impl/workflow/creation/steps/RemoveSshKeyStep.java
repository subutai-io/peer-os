package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


public class RemoveSshKeyStep
{
    private final String sshKey;
    private final EnvironmentImpl environment;
    private final NetworkManager networkManager;


    public RemoveSshKeyStep( final String sshKey, final EnvironmentImpl environment,
                             final NetworkManager networkManager )
    {
        this.sshKey = sshKey;
        this.environment = environment;
        this.networkManager = networkManager;
    }


    public void execute() throws NetworkManagerException
    {
        if ( !Strings.isNullOrEmpty( sshKey ) )
        {
            environment.removeSshKey( sshKey );

            Set<ContainerHost> ch = Sets.newHashSet();
            ch.addAll( environment.getContainerHosts() );

            //add ssh key to each environment container
            networkManager.removeSshKeyFromAuthorizedKeys( ch, sshKey );
        }
    }
}

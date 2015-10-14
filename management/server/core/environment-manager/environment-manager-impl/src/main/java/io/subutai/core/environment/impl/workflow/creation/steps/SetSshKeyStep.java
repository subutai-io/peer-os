package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


public class SetSshKeyStep
{
    private final String sshKey;
    private final EnvironmentImpl environment;
    private final NetworkManager networkManager;


    public SetSshKeyStep( final String sshKey, final EnvironmentImpl environment, final NetworkManager networkManager )
    {
        this.sshKey = sshKey;
        this.environment = environment;
        this.networkManager = networkManager;
    }


    public void execute() throws NetworkManagerException
    {
        if ( !Strings.isNullOrEmpty( sshKey ) )
        {
            String oldSshKey = environment.getSshKey();

            environment.saveSshKey( sshKey );
            Set<ContainerHost> ch = Sets.newHashSet();
            ch.addAll( environment.getContainerHosts() );

            if ( Strings.isNullOrEmpty( sshKey ) && !Strings.isNullOrEmpty( oldSshKey ) )
            {
                //remove old key from containers
                networkManager.removeSshKeyFromAuthorizedKeys( ch, oldSshKey );
            }
            else if ( !Strings.isNullOrEmpty( sshKey ) && Strings.isNullOrEmpty( oldSshKey ) )
            {
                //insert new key to containers
                networkManager.addSshKeyToAuthorizedKeys( ch, sshKey );
            }
            else if ( !Strings.isNullOrEmpty( sshKey ) && !Strings.isNullOrEmpty( oldSshKey ) )
            {
                //replace old ssh key with new one
                networkManager.replaceSshKeyInAuthorizedKeys( ch, oldSshKey, sshKey );
            }
        }
    }
}

package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


public class RegisterSshStep
{

    private final EnvironmentImpl environment;
    private final NetworkManager networkManager;


    public RegisterSshStep( final EnvironmentImpl environment, final NetworkManager networkManager )
    {
        this.environment = environment;
        this.networkManager = networkManager;
    }


    public void execute() throws NetworkManagerException
    {
        configureSsh( environment.getContainerHosts() );
    }


    public void configureSsh( final Set<EnvironmentContainerHost> containerHosts ) throws NetworkManagerException
    {
        Map<Integer, Set<EnvironmentContainerHost>> sshGroups = Maps.newHashMap();

        //group containers by ssh group
        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            int sshGroupId = ( ( EnvironmentContainerImpl ) containerHost ).getSshGroupId();
            Set<EnvironmentContainerHost> groupedContainers = sshGroups.get( sshGroupId );

            if ( groupedContainers == null )
            {
                groupedContainers = Sets.newHashSet();
                sshGroups.put( sshGroupId, groupedContainers );
            }

            groupedContainers.add( containerHost );
        }

        //configure ssh on each group
        for ( Map.Entry<Integer, Set<EnvironmentContainerHost>> sshGroup : sshGroups.entrySet() )
        {
            int sshGroupId = sshGroup.getKey();
            Set<EnvironmentContainerHost> groupedContainers = sshGroup.getValue();

            //ignore group ids <= 0
            if ( sshGroupId > 0 )
            {
                networkManager.exchangeSshKeys( groupedContainers );
            }
        }
    }
}

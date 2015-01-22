package org.safehaus.subutai.core.env.impl;


import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.EnvironmentStatus;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.ContainerHostNotFoundException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;
import org.safehaus.subutai.core.env.impl.builder.TopologyBuilder;
import org.safehaus.subutai.core.env.impl.dao.BlueprintDataService;
import org.safehaus.subutai.core.env.impl.dao.EnvironmentContainerDataService;
import org.safehaus.subutai.core.env.impl.dao.EnvironmentDataService;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    private final TopologyBuilder topologyBuilder;

    //************* DaoManager ******************
    private final DaoManager daoManager;

    //************* Data Managers ******************
    private EnvironmentDataService environmentDataService;
    private EnvironmentContainerDataService environmentContainerDataService;
    private BlueprintDataService blueprintDataService;


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                   final NetworkManager networkManager, final DaoManager daoManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( daoManager );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.daoManager = daoManager;
        this.topologyBuilder = new TopologyBuilder( templateRegistry, peerManager );
    }


    //************* Init Data Managers ******************
    public void init()
    {
        try
        {
            this.blueprintDataService = new BlueprintDataService( daoManager );
            this.environmentDataService = new EnvironmentDataService( daoManager );
            this.environmentContainerDataService = new EnvironmentContainerDataService( daoManager );
        }
        catch ( SQLException e )
        {
        }
    }


    @Override
    public Set<Environment> getEnvironments()
    {
        Set<Environment> environments = Sets.newHashSet();
        environments.addAll( environmentDataService.getAll() );

        for ( Environment environment : environments )
        {
            ( ( EnvironmentImpl ) environment ).setDataService( environmentDataService );
            setContainersTransitiveFields( environment.getContainerHosts() );
        }

        return environments;
    }


    @Override
    public Environment findEnvironment( final UUID environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = environmentDataService.find( environmentId.toString() );
        if ( environment == null )
        {
            throw new EnvironmentNotFoundException();
        }

        //set dataservice
        environment.setDataService( environmentDataService );

        //set container's transient fields
        setContainersTransitiveFields( environment.getContainerHosts() );

        return environment;
    }


    @Override
    public Environment createEnvironment( final String name, final Topology topology )
            throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        EnvironmentImpl environment = new EnvironmentImpl( name );

        environmentDataService.persist( environment );

        environment.setDataService( environmentDataService );

        try
        {
            topologyBuilder.build( environment, topology );

            configureHosts( environment.getContainerHosts() );

            configureSsh( environment.getContainerHosts() );
        }
        catch ( EnvironmentBuildException | NetworkManagerException e )
        {
            environment.setStatus( EnvironmentStatus.UNHEALTHY );

            throw new EnvironmentCreationException( e );
        }

        environment.setStatus( EnvironmentStatus.HEALTHY );

        //set container's transient fields
        setContainersTransitiveFields( environment.getContainerHosts() );

        return environment;
    }


    @Override
    public void destroyEnvironment( final UUID environmentId )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        Set<ContainerHost> containers = Sets.newHashSet( environment.getContainerHosts() );

        for ( ContainerHost container : containers )
        {
            try
            {
                container.dispose();
                environment.removeContainer( container.getId() );
                environmentContainerDataService.remove( container.getId().toString() );
            }
            catch ( ContainerHostNotFoundException | PeerException e )
            {
                environment.setStatus( EnvironmentStatus.UNHEALTHY );

                throw new EnvironmentDestructionException( e );
            }
        }

        environmentDataService.remove( environmentId.toString() );
    }


    @Override
    public Environment growEnvironment( final UUID environmentId, final Topology topology )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        try
        {
            topologyBuilder.build( environment, topology );

            configureHosts( environment.getContainerHosts() );

            configureSsh( environment.getContainerHosts() );

            //set container's transient fields
            setContainersTransitiveFields( environment.getContainerHosts() );
        }
        catch ( EnvironmentBuildException | NetworkManagerException e )
        {
            environment.setStatus( EnvironmentStatus.UNHEALTHY );

            throw new EnvironmentModificationException( e );
        }

        return environment;
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( containerHost, "Invalid container host" );

        EnvironmentImpl environment =
                ( EnvironmentImpl ) findEnvironment( UUID.fromString( containerHost.getEnvironmentId() ) );

        try
        {
            containerHost.dispose();

            environment.removeContainer( containerHost.getId() );

            environmentContainerDataService.remove( containerHost.getId().toString() );
        }
        catch ( ContainerHostNotFoundException | PeerException e )
        {
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public NodeGroup newNodeGroup( final String name, final String templateName, final String domainName,
                                   final int numberOfContainers, final int sshGroupId, final int hostsGroupId,
                                   final PlacementStrategy containerPlacementStrategy )
    {
        return new NodeGroupImpl( name, templateName, domainName, numberOfContainers, sshGroupId, hostsGroupId,
                containerPlacementStrategy );
    }


    @Override
    public Topology newTopology()
    {
        return new TopologyImpl();
    }


    @Override
    public void removeEnvironment( final UUID environmentId ) throws EnvironmentNotFoundException
    {
        Environment environment = findEnvironment( environmentId );

        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            environmentContainerDataService.remove( containerHost.getId().toString() );
        }

        environmentDataService.remove( environmentId.toString() );
    }


    private void setContainersTransitiveFields( Set<ContainerHost> containers )
    {
        //set container's transient fields
        for ( ContainerHost containerHost : containers )
        {
            ( ( EnvironmentContainerImpl ) containerHost ).setDataService( environmentContainerDataService );
            ( ( EnvironmentContainerImpl ) containerHost ).setPeer( peerManager.getPeer( containerHost.getPeerId() ) );
        }
    }


    private void configureSsh( Set<ContainerHost> containerHosts ) throws NetworkManagerException
    {
        Map<Integer, Set<ContainerHost>> sshGroups = Maps.newHashMap();

        //group containers by ssh group
        for ( ContainerHost containerHost : containerHosts )
        {
            int sshGroupId = ( ( EnvironmentContainerImpl ) containerHost ).getSshGroupId();
            Set<ContainerHost> groupedContainers = sshGroups.get( sshGroupId );

            if ( groupedContainers == null )
            {
                groupedContainers = Sets.newHashSet();
                sshGroups.put( sshGroupId, groupedContainers );
            }

            groupedContainers.add( containerHost );
        }

        //configure ssh on each group
        for ( Map.Entry<Integer, Set<ContainerHost>> sshGroup : sshGroups.entrySet() )
        {
            int sshGroupId = sshGroup.getKey();
            Set<ContainerHost> groupedContainers = sshGroup.getValue();

            //ignore group ids <= 0
            if ( sshGroupId > 0 )
            {
                networkManager.exchangeSshKeys( groupedContainers );
            }
        }
    }


    private void configureHosts( Set<ContainerHost> containerHosts ) throws NetworkManagerException
    {
        Map<Integer, Set<ContainerHost>> hostGroups = Maps.newHashMap();

        //group containers by host group
        for ( ContainerHost containerHost : containerHosts )
        {
            int hostGroupId = ( ( EnvironmentContainerImpl ) containerHost ).getHostsGroupId();
            Set<ContainerHost> groupedContainers = hostGroups.get( hostGroupId );

            if ( groupedContainers == null )
            {
                groupedContainers = Sets.newHashSet();
                hostGroups.put( hostGroupId, groupedContainers );
            }

            groupedContainers.add( containerHost );
        }

        //configure hosts on each group
        for ( Map.Entry<Integer, Set<ContainerHost>> hostGroup : hostGroups.entrySet() )
        {
            int hostGroupId = hostGroup.getKey();
            Set<ContainerHost> groupedContainers = hostGroup.getValue();

            //ignore group ids <= 0
            if ( hostGroupId > 0 )
            {
                //assume that inside one host group the domain name must be the same for all containers
                //so pick one container's domain name as the group domain name
                networkManager.registerHosts( groupedContainers,
                        ( ( EnvironmentContainerImpl ) groupedContainers.iterator().next() ).getDomainName() );
            }
        }
    }
}

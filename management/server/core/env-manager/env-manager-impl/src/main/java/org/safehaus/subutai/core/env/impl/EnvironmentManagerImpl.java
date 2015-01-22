package org.safehaus.subutai.core.env.impl;


import java.sql.SQLException;
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
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final TopologyBuilder topologyBuilder;

    //************* DaoManager ******************
    private DaoManager daoManager;

    //************* Data Managers ******************
    private EnvironmentDataService environmentDataService;
    private EnvironmentContainerDataService environmentContainerDataService;
    private BlueprintDataService blueprintDataService;


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
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
            //TODO think of updating environment in topology builder after every node group is created
            environment.addContainers( topologyBuilder.build( topology ) );
        }
        catch ( EnvironmentBuildException e )
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
            //TODO think of passing environment to topologyBuilder and adding every succeeded node group and saving
            Set<EnvironmentContainerImpl> newContainers = topologyBuilder.build( topology );

            environment.addContainers( newContainers );

            //set container's transient fields
            setContainersTransitiveFields( Sets.<ContainerHost>newHashSet( newContainers ) );
        }
        catch ( EnvironmentBuildException e )
        {
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


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }
}

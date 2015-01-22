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
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


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
            this.blueprintDataService            = new BlueprintDataService( daoManager );
            this.environmentDataService          = new EnvironmentDataService( daoManager);
            this.environmentContainerDataService = new EnvironmentContainerDataService( daoManager );
        }
        catch ( SQLException e )
        {
        }
    }

    @Override
    public Environment findEnvironment( final UUID environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        //TODO get environment from database
        //TODO set dataservice to environment
        //TODO set peer and data service on each container

        return null;
    }


    @Override
    public Environment createEnvironment( final String name, final Topology topology )
            throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        EnvironmentImpl environment = new EnvironmentImpl( name );

        //TODO save environment to database here

        try
        {
            //TODO think of updating environment in topology builder after every node group is created
            environment.addContainers( topologyBuilder.build( topology ) );
        }
        catch ( EnvironmentBuildException e )
        {
            //TODO update env status here

            throw new EnvironmentCreationException( e );
        }

        //TODO update env status here

        return environment;
    }


    @Override
    public void destroyEnvironment( final UUID environmentId )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        Set<ContainerHost> containers = environment.getContainerHosts();

        for ( ContainerHost container : containers )
        {
            try
            {
                container.dispose();
                environment.removeContainer( container.getId() );
            }
            catch ( ContainerHostNotFoundException | PeerException e )
            {
                //TODO update env status here

                throw new EnvironmentDestructionException( e );
            }
        }

        //TODO remove environment from database
    }


    @Override
    public void growEnvironment( final UUID environmentId, final Topology topology )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        try
        {
            //TODO think of passing environment to topologyBuilder and adding every succeeded node group and saving
            environment.addContainers( topologyBuilder.build( topology ) );
        }
        catch ( EnvironmentBuildException e )
        {
            throw new EnvironmentModificationException( e );
        }
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
        }
        catch ( ContainerHostNotFoundException | PeerException e )
        {
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public NodeGroup newNodeGroup( final String name, final String templateName, final String domainName,
                                   final int numberOfNodes, final int sshGroupId, final int hostsGroupId,
                                   final PlacementStrategy nodePlacementStrategy )
    {
        return new NodeGroupImpl( name, templateName, domainName, numberOfNodes, sshGroupId, hostsGroupId,
                nodePlacementStrategy );
    }


    @Override
    public Topology newTopology()
    {
        return new TopologyImpl();
    }


    public DaoManager getDaoManager()
    {
        return daoManager;
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }
}

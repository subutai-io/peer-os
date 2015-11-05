package io.subutai.core.environment.impl;


import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInfoModel;
import io.subutai.common.host.Interface;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.api.exception.EnvironmentSecurityException;
import io.subutai.core.environment.impl.dao.BlueprintDataService;
import io.subutai.core.environment.impl.dao.EnvironmentContainerDataService;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.construction.EnvironmentImportWorkflow;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.ContainerDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.EnvironmentDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentGrowingWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyModificationWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.tracker.api.Tracker;


@PermitAll
public class EnvironmentManagerImpl implements EnvironmentManager
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class );

    private static final String TRACKER_SOURCE = "Environment Manager";

    private final IdentityManager identityManager;
    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    private final Tracker tracker;
    private final TemplateRegistry templateRegistry;

    private final DaoManager daoManager;

    protected Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();

    protected EnvironmentDataService environmentDataService;
    protected EnvironmentContainerDataService environmentContainerDataService;
    protected BlueprintDataService blueprintDataService;

    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    @Override
    public Environment importEnvironment( final String name, final Topology topology,
                                          final Map<NodeGroup, Set<HostInfo>> containers, final String ssh,
                                          final Integer vlan ) throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        Map.Entry<NodeGroup, Set<HostInfo>> containersEntry = containers.entrySet().iterator().next();
        Iterator<HostInfo> hostIterator = containersEntry.getValue().iterator();

        String ip = "";

        while ( hostIterator.hasNext() && StringUtil.isStringNullOrEmpty( ip ) )
        {
            HostInfo sampleHostInfo = hostIterator.next();

            //TODO ip is chosen from first standing container host info
            for ( final Interface iface : sampleHostInfo.getInterfaces() )
            {
                if ( StringUtil.isStringNullOrEmpty( iface.getIp() ) )
                {
                    continue;
                }
                ip = iface.getIp() + "/24";
                break;
            }
        }

        if ( StringUtil.isStringNullOrEmpty( ip ) )
        {
            throw new EnvironmentCreationException( "Invalid environment ip range" );
        }

        //create empty environment
        final EnvironmentImpl environment = createEmptyEnvironment( name, ip, ssh );
        for ( Map.Entry<NodeGroup, Set<HostInfo>> entry : containers.entrySet() )
        {
            for ( HostInfo newHost : entry.getValue() )
            {
                environment.addContainers( Sets.newHashSet(
                        new EnvironmentContainerImpl( peerManager.getLocalPeer().getId(), peerManager.getLocalPeer(),
                                entry.getKey().getName(), new HostInfoModel( newHost ),
                                templateRegistry.getTemplate( entry.getKey().getTemplateName() ),
                                entry.getKey().getSshGroupId(), entry.getKey().getHostsGroupId(),
                                Common.DEFAULT_DOMAIN_NAME ) ) );
            }
        }
        TrackerOperation operationTracker = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Creating environment %s ", environment.getId() ) );

        EnvironmentImportWorkflow environmentImportWorkflow =
                getEnvironmentImportWorkflow( environment, topology, ssh, operationTracker );

        environmentImportWorkflow.start();
        //notify environment event listeners
        environmentImportWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                notifyOnEnvironmentCreated( environment );
            }
        } );


        return environment;
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public Environment createEnvironment( final String name, final Topology topology, final String subnetCidr,
                                          final String sshKey, final boolean async ) throws EnvironmentCreationException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subnetCidr ), "Invalid subnet CIDR" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        //create empty environment
        final EnvironmentImpl environment = createEmptyEnvironment( name, subnetCidr, sshKey );

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Creating environment %s ", environment.getId() ) );

        //launch environment creation workflow
        final EnvironmentCreationWorkflow environmentCreationWorkflow =
                getEnvironmentCreationWorkflow( environment, topology, sshKey, operationTracker );

        //start environment creation workflow
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentCreationWorkflow.start();
            }
        } );

        //notify environment event listeners
        environmentCreationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {

                notifyOnEnvironmentCreated( environment );
            }
        } );

        //wait
        if ( !async )
        {
            environmentCreationWorkflow.join();

            if ( environmentCreationWorkflow.getError() != null )
            {
                throw new EnvironmentCreationException(
                        exceptionUtil.getRootCause( environmentCreationWorkflow.getError() ) );
            }
        }

        //return created environment
        return environment;
    }


//    @RolesAllowed( "Environment-Management|A|Write" ) @todo
    @Override
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                          final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Growing environment %s", environmentId ) );

        return growEnvironment( environmentId, topology, async, true, op );
    }


//    @RolesAllowed( "Environment-Management|A|Write" ) @todo
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                          final boolean async, final boolean checkAccess,
                                                          final TrackerOperation operationTracker )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final Set<EnvironmentContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );


        //launch environment growing workflow
        final EnvironmentGrowingWorkflow environmentGrowingWorkflow =
                getEnvironmentGrowingWorkflow( environment, topology, environment.getSshKey(), operationTracker );

        //start environment growing workflow
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentGrowingWorkflow.start();
            }
        } );

        //notify environment event listeners
        environmentGrowingWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Set<EnvironmentContainerHost> newContainers = Sets.newHashSet( environment.getContainerHosts() );
                    newContainers.removeAll( oldContainers );
                    notifyOnEnvironmentGrown( loadEnvironment( environment.getId(), checkAccess ), newContainers );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    LOG.error( "Error notifying environment event listeners", e );
                }
            }
        } );

        //wait
        if ( !async )
        {
            environmentGrowingWorkflow.join();

            if ( environmentGrowingWorkflow.getError() != null )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( environmentGrowingWorkflow.getError() ) );
            }
            else
            {
                Set<EnvironmentContainerHost> newContainers =
                        Sets.newHashSet( loadEnvironment( environment.getId() ).getContainerHosts() );
                newContainers.removeAll( oldContainers );
                return newContainers;
            }
        }

        return Sets.newHashSet();
    }


//    @RolesAllowed( "Environment-Management|A|Write" ) @todo
    @Override
    public void setSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Setting environment %s ssh key", environmentId ) );

        setSshKey( environmentId, sshKey, async, true, op );
    }


    public void setSshKey( final String environmentId, final String sshKey, final boolean async,
                           final boolean checkAccess, final TrackerOperation operationTracker )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        final SshKeyModificationWorkflow sshKeyModificationWorkflow =
                getSshKeyModificationWorkflow( environment, sshKey, networkManager, operationTracker );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                sshKeyModificationWorkflow.start();
            }
        } );

        //wait
        if ( !async )
        {
            sshKeyModificationWorkflow.join();

            if ( sshKeyModificationWorkflow.getError() != null )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( sshKeyModificationWorkflow.getError() ) );
            }
        }
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    @Override
    public void destroyEnvironment( final String environmentId, final boolean async,
                                    final boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Destroying environment %s", environmentId ) );

        destroyEnvironment( environmentId, async, forceMetadataRemoval, true, op );
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    public void destroyEnvironment( final String environmentId, boolean async, final boolean forceMetadataRemoval,
                                    final boolean checkAccess, final TrackerOperation operationTracker )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentDestructionException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final EnvironmentDestructionWorkflow environmentDestructionWorkflow =
                getEnvironmentDestructionWorkflow( peerManager, this, environment, forceMetadataRemoval,
                        operationTracker );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentDestructionWorkflow.start();
            }
        } );

        environmentDestructionWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    loadEnvironment( environmentId );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    notifyOnEnvironmentDestroyed( environmentId );
                }
            }
        } );

        //wait
        if ( !async )
        {
            environmentDestructionWorkflow.join();

            if ( environmentDestructionWorkflow.getError() != null )
            {
                throw new EnvironmentDestructionException(
                        exceptionUtil.getRootCause( environmentDestructionWorkflow.getError() ) );
            }
        }
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    @Override
    public void destroyContainer( final String environmentId, final String containerId, final boolean async,
                                  final boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );

        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Destroying container %s", containerId ) );

        destroyContainer( environmentId, containerId, async, forceMetadataRemoval, true, op );
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    public void destroyContainer( final String environmentId, final String containerId, final boolean async,
                                  final boolean forceMetadataRemoval, final boolean checkAccess,
                                  final TrackerOperation operationTracker )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        ContainerHost environmentContainer;
        try
        {
            environmentContainer = environment.getContainerHostById( containerId );
        }
        catch ( ContainerHostNotFoundException e )
        {
            operationTracker.addLogFailed( String.format( "Container not registered: %s", e.getMessage() ) );

            throw new EnvironmentModificationException( e );
        }


        final ContainerDestructionWorkflow containerDestructionWorkflow =
                getContainerDestructionWorkflow( this, environment, environmentContainer, forceMetadataRemoval,
                        operationTracker );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                containerDestructionWorkflow.start();
            }
        } );

        //wait
        if ( !async )
        {
            containerDestructionWorkflow.join();

            if ( containerDestructionWorkflow.getError() != null )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( containerDestructionWorkflow.getError() ) );
            }
        }
    }

    @PermitAll
    @Override
    public Environment loadEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        return loadEnvironment( environmentId, false );
    }


    @Override
    public String getDefaultDomainName()
    {
        return Common.DEFAULT_DOMAIN_NAME;
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    @Override
    public void removeEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        removeEnvironment( environmentId, true );
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    public void removeEnvironment( final String environmentId, final boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        Environment environment = loadEnvironment( environmentId, checkAccess );

        environmentDataService.remove( ( EnvironmentImpl ) environment );

        notifyOnEnvironmentDestroyed( environmentId );
    }

    @PermitAll
    @Override
    public Set<Environment> getEnvironments()
    {
        Set<Environment> environments = Sets.newHashSet();
        environments.addAll( environmentDataService.getAll() );

        for ( Environment environment : environments )
        {
            setEnvironmentTransientFields( environment );
            setContainersTransientFields( environment );
        }

        //@todo remove temporarily frontend-ui
//        if ( !isUserAdmin() )
//        {
//            Long userId = getUserId();
//            for ( Iterator<Environment> iterator = environments.iterator(); iterator.hasNext(); )
//            {
//                final Environment environment = iterator.next();
//                if ( !Objects.equals( environment.getUserId(), userId ) )
//                {
//                    iterator.remove();
//                }
//            }
//        }

        return environments;
    }


//    @RolesAllowed( "Environment-Management|A|Write" ) @todo
    @Override
    public void saveBlueprint( final Blueprint blueprint ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( blueprint, "Invalid blueprint" );

        blueprintDataService.persist( blueprint );
    }


//    @RolesAllowed( "Environment-Management|A|Delete" ) @todo
    @Override
    public void removeBlueprint( final UUID blueprintId ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( blueprintId, "Invalid blueprint id" );

        blueprintDataService.remove( blueprintId );
    }


    @PermitAll
    @Override
    public Set<Blueprint> getBlueprints() throws EnvironmentManagerException
    {
        return blueprintDataService.getAll();
    }


//    @RolesAllowed( "Environment-Management|A|Update" ) @todo
    @Override
    public void updateEnvironmentContainersMetadata( final String environmentId ) throws EnvironmentManagerException
    {
        try
        {
            Environment environment = loadEnvironment( environmentId );
            Set<EnvironmentContainerHost> containerHosts = environment.getContainerHosts();

            for ( final ContainerHost containerHost : containerHosts )
            {
                try
                {
                    HostInfo hostInfo = containerHost.getPeer().getContainerHostInfoById( containerHost.getId() );

                    EnvironmentContainerImpl environmentContainer =
                            environmentContainerDataService.find( containerHost.getId() );
                    environmentContainer.setHostname( hostInfo.getHostname() );
                    environmentContainer.setNetInterfaces( hostInfo.getInterfaces() );

                    environmentContainerDataService.update( environmentContainer );
                }
                catch ( Exception e )
                {
                    LOG.error( "Couldn't get container host info from hosting peer", e );
                }
            }
        }
        catch ( EnvironmentNotFoundException e )
        {
            throw new EnvironmentManagerException(
                    String.format( "Couldn't find environment by id: %s", environmentId ), e );
        }
    }


//    @RolesAllowed( "Environment-Management|A|Update" ) @todo
    @Override
    public void assignEnvironmentDomain( final String environmentId, final String newDomain,
                                         final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newDomain ), "Invalid domain" );
        Preconditions.checkArgument( newDomain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );
        Preconditions.checkNotNull( domainLoadBalanceStrategy );

        TrackerOperation operationTracker = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Assigning environment %s domain", environmentId ) );

        modifyEnvironmentDomain( environmentId, newDomain, domainLoadBalanceStrategy, operationTracker, true,
                sslCertPath );
    }


//    @RolesAllowed( "Environment-Management|A|Update" ) @todo
    @Override
    public void removeEnvironmentDomain( final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        TrackerOperation operationTracker = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Removing environment %s domain", environmentId ) );

        modifyEnvironmentDomain( environmentId, null, null, operationTracker, true, null );
    }


    public void modifyEnvironmentDomain( final String environmentId, final String domain,
                                         final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                                         final TrackerOperation operationTracker, boolean checkAccess,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkNotNull( operationTracker );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        try
        {
            if ( Strings.isNullOrEmpty( domain ) )
            {
                peerManager.getLocalPeer().removeVniDomain( environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer()
                           .setVniDomain( environment.getVni(), domain, domainLoadBalanceStrategy, sslCertPath );
            }

            operationTracker.addLogDone( "Environment domain modified" );
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed( String.format( "Error modifying environment domain: %s", e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }


    @PermitAll
    @Override
    public String getEnvironmentDomain( final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, true );

        try
        {
            return peerManager.getLocalPeer().getVniDomain( environment.getVni() );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( "Error obtaining environment domain", e );
        }
    }


    @PermitAll
    @Override
    public boolean isContainerInEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, true );

        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );

            return peerManager.getLocalPeer().isIpInVniDomain(
                    containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ), environment.getVni() );
        }
        catch ( ContainerHostNotFoundException | PeerException e )
        {
            throw new EnvironmentManagerException( "Error checking container domain", e );
        }
    }


//    @RolesAllowed( "Environment-Management|A|Update" ) @todo
    @Override
    public void addContainerToEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        TrackerOperation operationTracker = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Adding container %s to environment domain", containerHostId ) );

        toggleContainerDomain( containerHostId, environmentId, true, operationTracker, true );
    }


//    @RolesAllowed( "Environment-Management|A|Update" ) @todo
    @Override
    public void removeContainerFromEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        TrackerOperation operationTracker = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Removing container %s from environment domain", containerHostId ) );

        toggleContainerDomain( containerHostId, environmentId, false, operationTracker, true );
    }


    public void toggleContainerDomain( final String containerHostId, final String environmentId, final boolean add,
                                       final TrackerOperation operationTracker, final boolean checkAccess )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        ContainerHost containerHost = environment.getContainerHostById( containerHostId );
        try
        {
            if ( add )
            {
                peerManager.getLocalPeer()
                           .addIpToVniDomain( containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
                                   environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer().removeIpFromVniDomain(
                        containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
                        environment.getVni() );
            }

            operationTracker.addLogDone(
                    String.format( "Container is %s environment domain", add ? "included in" : "excluded from" ) );
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed( String.format( "Error %s environment domain: %s",
                    add ? "including container in" : "excluding container from", e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }

    //************ utility methods

    @PermitAll
    protected EnvironmentCreationWorkflow getEnvironmentCreationWorkflow( final EnvironmentImpl environment,
                                                                          final Topology topology, final String sshKey,
                                                                          final TrackerOperation operationTracker )
    {
        return new EnvironmentCreationWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, this, networkManager,
                peerManager, environment, topology, sshKey, operationTracker );
    }


    protected EnvironmentImportWorkflow getEnvironmentImportWorkflow( final EnvironmentImpl environment,
                                                                      final Topology topology, final String sshKey,
                                                                      final TrackerOperation tracker )
    {
        return new EnvironmentImportWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, this, networkManager,
                peerManager, environment, topology, sshKey, tracker );
    }


    protected EnvironmentGrowingWorkflow getEnvironmentGrowingWorkflow( final EnvironmentImpl environment,
                                                                        final Topology topology, final String sshKey,
                                                                        final TrackerOperation operationTracker )
    {
        return new EnvironmentGrowingWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, networkManager,
                peerManager, environment, topology, sshKey, operationTracker, this );
    }


    protected EnvironmentDestructionWorkflow getEnvironmentDestructionWorkflow( final PeerManager peerManager,
                                                                                final EnvironmentManagerImpl
                                                                                        environmentManager,
                                                                                final EnvironmentImpl environment,
                                                                                final boolean forceMetadataRemoval,
                                                                                final TrackerOperation
                                                                                        operationTracker )
    {
        return new EnvironmentDestructionWorkflow( environmentManager, environment, forceMetadataRemoval,
                operationTracker );
    }


    protected ContainerDestructionWorkflow getContainerDestructionWorkflow(
            final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
            final ContainerHost containerHost, final boolean forceMetadataRemoval,
            final TrackerOperation operationTracker )
    {
        return new ContainerDestructionWorkflow( environmentManager, environment, containerHost, forceMetadataRemoval,
                operationTracker );
    }


    protected SshKeyModificationWorkflow getSshKeyModificationWorkflow( final EnvironmentImpl environment,
                                                                        final String sshKey,
                                                                        final NetworkManager networkManager,
                                                                        final TrackerOperation operationTracker )
    {
        return new SshKeyModificationWorkflow( environment, sshKey, networkManager, operationTracker, this );
    }


    @PermitAll
    protected Environment loadEnvironment( final String environmentId, boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = environmentDataService.find( environmentId );

        if ( environment == null )
        {
            throw new EnvironmentNotFoundException();
        }

        //check access to environment
        if ( checkAccess )
        {
            checkAccess( environment );
        }

        //set environment's transient fields
        setEnvironmentTransientFields( environment );

        //set container's transient fields
        setContainersTransientFields( environment );

        return environment;
    }


    protected void checkAccess( final Environment environment )
    {
        if ( !( isUserAdmin() || Objects.equals( environment.getUserId(), getUserId() ) ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to environment %s is denied", environment.getName() ) );
        }
    }


    protected boolean isUserAdmin()
    {
        return true;//getUser().isAdmin();
    }


    public void setEnvironmentTransientFields( final Environment environment )
    {
        ( ( EnvironmentImpl ) environment ).setEnvironmentManager( this );
    }


    public void setContainersTransientFields( final Environment environment )
    {
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {

            ( ( EnvironmentContainerImpl ) containerHost ).setEnvironmentManager( this );

            String peerId = containerHost.getPeerId();
            Peer peer = peerManager.getPeer( peerId );

            ( ( EnvironmentContainerImpl ) containerHost ).setPeer( peer );
        }
    }


    public void registerListener( final EnvironmentEventListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void unregisterListener( final EnvironmentEventListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }


    @PermitAll
    public void notifyOnEnvironmentCreated( final Environment environment )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onEnvironmentCreated( environment );
                }
            } );
        }
    }


    @PermitAll
    public void notifyOnEnvironmentGrown( final Environment environment,
                                          final Set<EnvironmentContainerHost> containers )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onEnvironmentGrown( environment, containers );
                }
            } );
        }
    }


    @PermitAll
    public void notifyOnContainerDestroyed( final Environment environment, final String containerId )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onContainerDestroyed( environment, containerId );
                }
            } );
        }
    }


    @PermitAll
    @Override
    public void notifyOnContainerStateChanged( final Environment environment, final ContainerHost containerHost )
    {
        saveOrUpdate( environment );
    }


    @PermitAll
    public void notifyOnEnvironmentDestroyed( final String environmentId )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onEnvironmentDestroyed( environmentId );
                }
            } );
        }
    }


//    @RolesAllowed( "Environment-Management|A|Write" ) @todo
    protected EnvironmentImpl createEmptyEnvironment( final String name, final String subnetCidr, final String sshKey )
    {

        EnvironmentImpl environment =
                new EnvironmentImpl( name, subnetCidr, sshKey, getUserId(), peerManager.getLocalPeerInfo().getId() );

        environment = saveOrUpdate( environment );

        setEnvironmentTransientFields( environment );

        notifyOnEnvironmentCreated( environment );

        return environment;
    }


    protected User getUser()
    {
        //User user = identityManager.getUser();
        //User user = identityManager.getUser( "admin" ); @todo ui workaround

        //if ( user == null )
        {
            throw new EnvironmentSecurityException( "User not authenticated" );
        }

        //return user;
    }


    protected Long getUserId()
    {
        return (long)0;//getUser().getId();
    }


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                   final NetworkManager networkManager, final DaoManager daoManager,
                                   final IdentityManager identityManager, final Tracker tracker )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( tracker );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.daoManager = daoManager;
        this.identityManager = identityManager;
        this.tracker = tracker;
    }


    public void init() throws SQLException
    {
        this.blueprintDataService = new BlueprintDataService( daoManager );
        this.environmentDataService = new EnvironmentDataService( daoManager );
        this.environmentContainerDataService = new EnvironmentContainerDataService( daoManager );
    }


    public void dispose()
    {
        executor.shutdown();
    }


    public Peer resolvePeer( final String peerId )
    {
        return peerManager.getPeer( peerId );
    }


    public EnvironmentImpl saveOrUpdate( final Environment environment )
    {
        EnvironmentImpl env = environmentDataService.saveOrUpdate( environment );
        setEnvironmentTransientFields( env );
        setContainersTransientFields( env );
        return env;
    }


    public void remove( final EnvironmentImpl environment )
    {
        environmentDataService.remove( environment );
    }
}

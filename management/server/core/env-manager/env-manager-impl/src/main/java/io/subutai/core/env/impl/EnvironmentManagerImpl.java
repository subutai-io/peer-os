package io.subutai.core.env.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.InterfacePattern;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.env.api.EnvironmentEventListener;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.env.api.exception.EnvironmentCreationException;
import io.subutai.core.env.api.exception.EnvironmentDestructionException;
import io.subutai.core.env.api.exception.EnvironmentManagerException;
import io.subutai.core.env.api.exception.EnvironmentSecurityException;
import io.subutai.core.env.impl.builder.EnvironmentBuilder;
import io.subutai.core.env.impl.dao.BlueprintDataService;
import io.subutai.core.env.impl.dao.EnvironmentContainerDataService;
import io.subutai.core.env.impl.dao.EnvironmentDataService;
import io.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.EnvironmentBuildException;
import io.subutai.core.env.impl.exception.ResultHolder;
import io.subutai.core.env.impl.tasks.Awaitable;
import io.subutai.core.env.impl.tasks.CreateEnvironmentTask;
import io.subutai.core.env.impl.tasks.DestroyContainerTask;
import io.subutai.core.env.impl.tasks.DestroyEnvironmentTask;
import io.subutai.core.env.impl.tasks.GrowEnvironmentTask;
import io.subutai.core.env.impl.tasks.SetContainerDomainTask;
import io.subutai.core.env.impl.tasks.SetDomainTask;
import io.subutai.core.env.impl.tasks.SetSshKeyTask;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.tracker.api.Tracker;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentManagerImpl.class );
    public static final String PEER_SUBNET_MASK = "255.255.255.0";
    private static final String TRACKER_SOURCE = "Environment Manager";
    private static final int N2N_PORT = 5000;

    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();
    private final String defaultDomain;
    private final IdentityManager identityManager;
    private final SecurityManager securityManager;
    private final Tracker tracker;

    protected Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();

    private final DaoManager daoManager;

    protected EnvironmentBuilder environmentBuilder;
    protected EnvironmentDataService environmentDataService;
    protected EnvironmentContainerDataService environmentContainerDataService;
    protected BlueprintDataService blueprintDataService;


    @Override
    public void saveBlueprint( final Blueprint blueprint ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( blueprint, "Invalid blueprint" );

        blueprintDataService.persist( blueprint );
    }


    @Override
    public void removeBlueprint( final UUID blueprintId ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( blueprintId, "Invalid blueprint id" );

        blueprintDataService.remove( blueprintId );
    }


    @Override
    public Set<Blueprint> getBlueprints() throws EnvironmentManagerException
    {
        return blueprintDataService.getAll();
    }


    @Override
    public String getDefaultDomainName()
    {
        return defaultDomain;
    }


    @Override
    public void updateEnvironmentContainersMetadata( final String environmentId ) throws EnvironmentManagerException
    {
        try
        {
            Environment environment = findEnvironment( environmentId );
            Set<ContainerHost> containerHosts = environment.getContainerHosts();

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
                    LOGGER.error( "Couldn't get container host info from hosting peer", e );
                }
            }
        }
        catch ( EnvironmentNotFoundException e )
        {
            throw new EnvironmentManagerException(
                    String.format( "Couldn't find environment by id: %s", environmentId ), e );
        }
    }


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

        if ( !isUserAdmin() )
        {
            Long userId = getUserId();
            for ( Iterator<Environment> iterator = environments.iterator(); iterator.hasNext(); )
            {
                final Environment environment = iterator.next();
                if ( !Objects.equals( environment.getUserId(), userId ) )
                {
                    iterator.remove();
                }
            }
        }

        return environments;
    }


    @Override
    public Environment findEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        return findEnvironment( environmentId, false );
    }


    protected Environment findEnvironment( final String environmentId, boolean checkAccess )
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


    protected EnvironmentImpl createEmptyEnvironment( final String name, final String subnetCidr, final String sshKey )
    {

        final EnvironmentImpl environment = new EnvironmentImpl( name, subnetCidr, sshKey, getUserId() );

        saveEnvironment( environment );

        setEnvironmentTransientFields( environment );

        notifyOnEnvironmentCreated( environment );

        return environment;
    }


    @Override
    public Environment createEnvironment( final String name, final Topology topology, final String subnetCidr,
                                          final String sshKey, final boolean async ) throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subnetCidr ), "Invalid subnet CIDR" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );


        final EnvironmentImpl environment = createEmptyEnvironment( name, subnetCidr, sshKey );


        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Creating environment %s ", environment.getId() ) );

        final ResultHolder<EnvironmentCreationException> resultHolder = new ResultHolder<>();


        Awaitable createEnvironmentTask =
                new CreateEnvironmentTask( peerManager.getLocalPeer(), this, environment, topology, resultHolder, op );

        executor.submit( createEnvironmentTask );

        if ( !async )
        {
            try
            {
                createEnvironmentTask.waitCompletion();

                if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentCreationException( e );
            }
        }

        try
        {
            return findEnvironment( environment.getId() );
        }
        catch ( EnvironmentNotFoundException e )
        {
            throw new EnvironmentCreationException( e );
        }
    }


    @Override
    public void destroyEnvironment( final String environmentId, final boolean async,
                                    final boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Destroying environment %s", environmentId ) );

        destroyEnvironment( environmentId, async, forceMetadataRemoval, true, op );
    }


    public void destroyEnvironment( final String environmentId, boolean async, final boolean forceMetadataRemoval,
                                    final boolean checkAccess, final TrackerOperation op )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            op.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentDestructionException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final ResultHolder<EnvironmentDestructionException> resultHolder = new ResultHolder<>();

        final Set<Throwable> exceptions = Sets.newHashSet();

        Awaitable destroyEnvironmentTask =
                new DestroyEnvironmentTask( this, environment, exceptions, resultHolder, forceMetadataRemoval,
                        peerManager.getLocalPeer(), op );

        executor.submit( destroyEnvironmentTask );

        if ( !async )
        {
            try
            {
                destroyEnvironmentTask.waitCompletion();

                if ( !exceptions.isEmpty() )
                {
                    throw new EnvironmentDestructionException(
                            String.format( "There were errors while destroying environment: %s", exceptions ) );
                }
                else if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentDestructionException( e );
            }
        }
    }


    @Override
    public Set<ContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                               final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Growing environment %s", environmentId ) );

        return growEnvironment( environmentId, topology, async, true, op );
    }


    public Set<ContainerHost> growEnvironment( final String environmentId, final Topology topology, final boolean async,
                                               final boolean checkAccess, final TrackerOperation op )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            op.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final Set<ContainerHost> newContainers = Sets.newHashSet();

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        Awaitable growEnvironmentTask =
                new GrowEnvironmentTask( this, environment, topology, resultHolder, newContainers, op );

        executor.submit( growEnvironmentTask );

        if ( !async )
        {
            try
            {
                growEnvironmentTask.waitCompletion();

                if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentModificationException( e );
            }
        }

        return newContainers;
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost, final boolean async,
                                  final boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( containerHost, "Invalid container host" );

        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Destroying container %s", containerHost.getHostname() ) );

        destroyContainer( containerHost, async, forceMetadataRemoval, true, op );
    }


    public void destroyContainer( final ContainerHost containerHost, final boolean async,
                                  final boolean forceMetadataRemoval, final boolean checkAccess,
                                  final TrackerOperation op )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( containerHost, "Invalid container host" );

        final EnvironmentImpl environment =
                ( EnvironmentImpl ) findEnvironment( containerHost.getEnvironmentId(), checkAccess );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            op.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        ContainerHost environmentContainer;
        try
        {
            environmentContainer = environment.getContainerHostById( containerHost.getId() );
        }
        catch ( ContainerHostNotFoundException e )
        {
            op.addLogFailed( String.format( "Container not registered: %s", e.getMessage() ) );

            throw new EnvironmentModificationException( e );
        }

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        Awaitable destroyContainerTask =
                new DestroyContainerTask( this, environment, environmentContainer, forceMetadataRemoval, resultHolder,
                        op );

        executor.submit( destroyContainerTask );

        if ( !async )
        {
            try
            {
                destroyContainerTask.waitCompletion();

                if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentModificationException( e );
            }
        }
    }


    @Override
    public void setSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Setting environment %s ssh key", environmentId ) );

        setSshKey( environmentId, sshKey, async, true, op );
    }


    public void setSshKey( final String environmentId, final String sshKey, final boolean async,
                           final boolean checkAccess, final TrackerOperation op )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        Awaitable setSshKeyTask = new SetSshKeyTask( environment, networkManager, resultHolder, sshKey, op );

        executor.submit( setSshKeyTask );

        if ( !async )
        {
            try
            {
                setSshKeyTask.waitCompletion();

                if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentModificationException( e );
            }
        }
    }


    @Override
    public void removeEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        removeEnvironment( environmentId, true );
    }


    public void removeEnvironment( final String environmentId, final boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        findEnvironment( environmentId, checkAccess );

        environmentDataService.remove( environmentId );

        notifyOnEnvironmentDestroyed( environmentId );
    }


    public Map<Peer, Set<Gateway>> getUsedGateways( final Set<Peer> peers ) throws EnvironmentManagerException
    {
        Map<Peer, Set<Gateway>> usedGateways = Maps.newHashMap();

        for ( Peer peer : peers )
        {
            try
            {
                usedGateways.put( peer, peer.getGateways() );
            }
            catch ( PeerException e )
            {
                throw new EnvironmentManagerException(
                        String.format( "Error obtaining gateways from peer %s", peer.getName() ), e );
            }
        }

        return usedGateways;
    }


    public long findFreeVni( final Set<Peer> peers ) throws EnvironmentManagerException
    {

        Set<Long> reservedVnis = Sets.newHashSet();
        for ( Peer peer : peers )
        {
            try
            {
                for ( Vni vni : peer.getReservedVnis() )
                {
                    reservedVnis.add( vni.getVni() );
                }
            }
            catch ( PeerException e )
            {
                throw new EnvironmentManagerException(
                        String.format( "Error obtaining reserved vnis from peer %s", peer.getName() ), e );
            }
        }

        int maxIterations = 10000;
        int currentIteration = 0;
        long vni;

        do
        {
            vni = ( long ) ( Math.random() * ( Common.MAX_VNI_ID - Common.MIN_VNI_ID ) ) + Common.MIN_VNI_ID;
            currentIteration++;
        }
        while ( reservedVnis.contains( vni ) && currentIteration < maxIterations );

        if ( reservedVnis.contains( vni ) )
        {
            throw new EnvironmentManagerException( "No free Vni found", null );
        }

        return vni;
    }


    public void saveEnvironment( final EnvironmentImpl environment )
    {
        environmentDataService.persist( environment );
    }


    public void build( final EnvironmentImpl environment, final Topology topology ) throws EnvironmentBuildException
    {
        environmentBuilder.build( environment, topology );
    }


    public void setEnvironmentTransientFields( final Environment environment )
    {
        ( ( EnvironmentImpl ) environment ).setDataService( environmentDataService );
        ( ( EnvironmentImpl ) environment ).setEnvironmentManager( this );
    }


    public void setContainersTransientFields( final Environment environment/*, final Set<ContainerHost> containers */ )
    {
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            ( ( EnvironmentContainerImpl ) containerHost ).setDataService( environmentContainerDataService );
            ( ( EnvironmentContainerImpl ) containerHost ).setEnvironmentManager( this );


            String peerId = containerHost.getPeerId();
            Peer peer = peerManager.getPeer( peerId );
            //
            //            String n2nIp = environment.findN2nIp( peerId );
            //            if ( n2nIp != null )
            //            {
            //                peer.getPeerInfo().setIp( n2nIp );
            //            }

            ( ( EnvironmentContainerImpl ) containerHost ).setPeer( peer );
        }
    }


    public void configureSsh( final Set<ContainerHost> containerHosts ) throws NetworkManagerException
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


    public void configureHosts( final Set<ContainerHost> containerHosts ) throws NetworkManagerException
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


    /** reverse proxy domain functions ** */

    @Override
    public void removeDomain( final String environmentId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Removing environment %s domain", environmentId ) );

        toggleEnvironmentDomain( environmentId, null, op, async, true );
    }


    @Override
    public void assignDomain( final String environmentId, final String newDomain, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( newDomain ), "Invalid domain" );
        Preconditions.checkArgument( newDomain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );

        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Assigning environment %s domain", environmentId ) );

        toggleEnvironmentDomain( environmentId, newDomain, op, async, true );
    }


    public void toggleEnvironmentDomain( final String environmentId, final String domain, final TrackerOperation op,
                                         final boolean async, boolean checkAccess )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        Awaitable setDomainTask = new SetDomainTask( environment, peerManager, resultHolder, op, domain );

        executor.submit( setDomainTask );

        if ( !async )
        {
            try
            {
                setDomainTask.waitCompletion();

                if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentModificationException( e );
            }
        }
    }


    @Override
    public String getDomain( final String environmentId ) throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        return getDomain( environmentId, true );
    }


    public String getDomain( final String environmentId, final boolean checkAccess )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        try
        {
            return peerManager.getLocalPeer().getVniDomain( environment.getVni() );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( "Error obtaining environment domain", e );
        }
    }


    @Override
    public boolean isContainerInDomain( final String containerHostId, final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {

        return isContainerInDomain( containerHostId, environmentId, true );
    }


    public boolean isContainerInDomain( final String containerHostId, final String environmentId,
                                        final boolean checkAccess )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( containerHostId, "Invalid container id" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

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


    @Override
    public void addContainerToDomain( final String containerHostId, final String environmentId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Adding container %s to environment domain", containerHostId ) );

        toggleContainerDomain( containerHostId, environmentId, true, op, async, true );
    }


    @Override
    public void removeContainerFromDomain( final String containerHostId, final String environmentId,
                                           final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Removing container %s from environment domain", containerHostId ) );

        toggleContainerDomain( containerHostId, environmentId, false, op, async, true );
    }


    public void toggleContainerDomain( final String containerHostId, final String environmentId, final boolean add,
                                       final TrackerOperation op, final boolean async, final boolean checkAccess )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkNotNull( containerHostId, "Invalid container id" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();


        ContainerHost containerHost = environment.getContainerHostById( containerHostId );


        Awaitable setContainerDomainTask =
                new SetContainerDomainTask( environment, containerHost, op, resultHolder, peerManager, add );

        executor.submit( setContainerDomainTask );

        if ( !async )
        {
            try
            {
                setContainerDomainTask.waitCompletion();

                if ( resultHolder.getResult() != null )
                {
                    throw resultHolder.getResult();
                }
            }
            catch ( InterruptedException e )
            {
                throw new EnvironmentModificationException( e );
            }
        }
    }


    /** reverse proxy domain functions end ** */


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


    public void notifyOnEnvironmentGrown( final Environment environment, final Set<ContainerHost> containers )
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


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                   final NetworkManager networkManager, final DaoManager daoManager,
                                   final String defaultDomain, final IdentityManager identityManager,
                                   final Tracker tracker, final SecurityManager securityManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( defaultDomain ) );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( tracker );

        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.daoManager = daoManager;
        this.defaultDomain = defaultDomain;
        this.environmentBuilder = new EnvironmentBuilder( templateRegistry, peerManager, defaultDomain );
        this.identityManager = identityManager;
        this.tracker = tracker;
        this.securityManager = securityManager;
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


    protected User getUser()
    {
        User user = identityManager.getUser();

        if ( user == null )
        {
            throw new EnvironmentSecurityException( "User not authenticated" );
        }

        return user;
    }


    protected boolean isUserAdmin()
    {
        return getUser().isAdmin();
    }


    protected Long getUserId()
    {
        return getUser().getId();
    }


    protected void checkAccess( final Environment environment )
    {
        if ( !( isUserAdmin() || Objects.equals( environment.getUserId(), getUserId() ) ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to environment %s is denied", environment.getName() ) );
        }
    }


    @Override
    public List<N2NConfig> setupN2NConnection( final Set<Peer> peers ) throws EnvironmentManagerException
    {
        Set<String> allSubnets = getSubnets( peers );
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( String.format( "Found %d peer subnets:", allSubnets.size() ) );
            for ( String s : allSubnets )
            {
                LOGGER.debug( s );
            }
        }
        String freeSubnet = findFreeEnvironmentSubnet( allSubnets );

        LOGGER.debug( String.format( "Free subnet for peer: %s", freeSubnet ) );
        try
        {
            if ( freeSubnet == null )
            {
                throw new IllegalStateException( "Could not calculate subnet." );
            }
            String superNodeIp = peerManager.getLocalPeer().getManagementHost().getExternalIp();
            String interfaceName = generateInterfaceName( freeSubnet );
            String communityName = generateCommunityName( freeSubnet );
            String sharedKey = UUID.randomUUID().toString();
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeSubnet, PEER_SUBNET_MASK ).getInfo();
            final String[] addresses = subnetInfo.getAllAddresses();
            int counter = 0;

            ExecutorService taskExecutor = Executors.newFixedThreadPool( peers.size() );

            ExecutorCompletionService<N2NConfig> executorCompletionService =
                    new ExecutorCompletionService<>( taskExecutor );


            List<N2NConfig> result = new ArrayList<>( peers.size() );
            for ( Peer peer : peers )
            {
                N2NConfig config = new N2NConfig( peer.getId(), superNodeIp, N2N_PORT, interfaceName, communityName,
                        addresses[counter], sharedKey );
                executorCompletionService.submit( new SetupN2NConnectionTask( peer, config ) );
                counter++;
            }

            for ( Peer peer : peers )
            {
                final Future<N2NConfig> f = executorCompletionService.take();
                N2NConfig config = f.get();
                result.add( config );
                counter++;
            }

            taskExecutor.shutdown();

            return result;
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new EnvironmentManagerException( "Could not create n2n tunnel.", e );
        }
    }


    @Override
    public void removeN2NConnection( final Environment environment ) throws EnvironmentManagerException
    {
        try
        {
            for ( PeerConf peerConf : environment.getPeerConfs() )
            {
                Peer peer = peerManager.getPeer( peerConf.getN2NConfig().getPeerId() );
                peer.removeN2NConnection( peerConf.getN2NConfig() );
            }
        }
        catch ( Exception e )
        {
            throw new EnvironmentManagerException( "Unable remove n2n tunnel.", e );
        }
    }


    private String generateCommunityName( final String freeSubnet )
    {
        return String.format( "com_%s", freeSubnet.replace( ".", "_" ) );
    }


    private String generateInterfaceName( final String freeSubnet )
    {
        return String.format( "n2n_%s", freeSubnet.replace( ".", "_" ) );
    }


    private String findFreeEnvironmentSubnet( final Set<String> allSubnets )
    {
        String result = null;
        int i = 11;
        int j = 0;

        while ( result == null && i < 254 )
        {
            String s = String.format( "10.%d.%d.0", i, j );
            if ( !allSubnets.contains( s ) )
            {
                result = s;
            }

            j++;
            if ( j > 254 )
            {
                i++;
                j = 0;
            }
        }

        return result;
    }


    private Set<String> getSubnets( final Set<Peer> allPeers )
    {
        Set<String> allSubnets = new HashSet<>();

        InterfacePattern peerSubnetsPattern = new InterfacePattern( "ip", "^10.*" );
        for ( Peer peer : allPeers )
        {
            Set<Interface> r = peer.getNetworkInterfaces( peerSubnetsPattern );

            Collection peerSubnets = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    Interface i = ( Interface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), PEER_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            allSubnets.addAll( peerSubnets );
        }

        return allSubnets;
    }


    private class SetupN2NConnectionTask implements Callable<N2NConfig>
    {
        private Peer peer;
        private N2NConfig n2NConfig;


        public SetupN2NConnectionTask( final Peer peer, final N2NConfig config )
        {
            this.peer = peer;
            this.n2NConfig = config;
        }


        @Override
        public N2NConfig call() throws Exception
        {
            peer.setupN2NConnection( n2NConfig );
            return n2NConfig;
        }
    }
}

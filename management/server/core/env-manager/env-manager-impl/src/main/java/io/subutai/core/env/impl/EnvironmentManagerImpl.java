package io.subutai.core.env.impl;


import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInfo;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
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
import io.subutai.core.env.impl.exception.EnvironmentTunnelException;
import io.subutai.core.env.impl.exception.ResultHolder;
import io.subutai.core.env.impl.tasks.CreateEnvironmentTask;
import io.subutai.core.env.impl.tasks.DestroyContainerTask;
import io.subutai.core.env.impl.tasks.DestroyEnvironmentTask;
import io.subutai.core.env.impl.tasks.GrowEnvironmentTask;
import io.subutai.core.env.impl.tasks.SetDomainTask;
import io.subutai.core.env.impl.tasks.SetSshKeyTask;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.tracker.api.Tracker;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentManagerImpl.class );
    private static final String TRACKER_SOURCE = "Environment Manager";

    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();
    private final String defaultDomain;
    private final IdentityManager identityManager;
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
    public void updateEnvironmentContainersMetadata( final UUID environmentId ) throws EnvironmentManagerException
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
                            environmentContainerDataService.find( containerHost.getId().toString() );
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
                    String.format( "Couldn't find environment by id: %s", environmentId.toString() ), e );
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
            setContainersTransientFields( environment.getContainerHosts() );
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
    public Environment findEnvironment( final UUID environmentId ) throws EnvironmentNotFoundException
    {
        return findEnvironment( environmentId, false );
    }


    protected Environment findEnvironment( final UUID environmentId, boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = environmentDataService.find( environmentId.toString() );

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
        setContainersTransientFields( environment.getContainerHosts() );

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


        CreateEnvironmentTask createEnvironmentTask =
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
    public void destroyEnvironment( final UUID environmentId, final boolean async, final boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Destroying environment %s", environmentId ) );

        destroyEnvironment( environmentId, async, forceMetadataRemoval, true, op );
    }


    public void destroyEnvironment( final UUID environmentId, boolean async, final boolean forceMetadataRemoval,
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

        DestroyEnvironmentTask destroyEnvironmentTask =
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
    public Set<ContainerHost> growEnvironment( final UUID environmentId, final Topology topology, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Growing environment %s", environmentId ) );

        return growEnvironment( environmentId, topology, async, true, op );
    }


    public Set<ContainerHost> growEnvironment( final UUID environmentId, final Topology topology, final boolean async,
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

        GrowEnvironmentTask growEnvironmentTask =
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
                ( EnvironmentImpl ) findEnvironment( UUID.fromString( containerHost.getEnvironmentId() ), checkAccess );

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

        DestroyContainerTask destroyContainerTask =
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
    public void setSshKey( final UUID environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Setting environment %s ssh key", environmentId ) );

        setSshKey( environmentId, sshKey, async, true, op );
    }


    public void setSshKey( final UUID environmentId, final String sshKey, final boolean async,
                           final boolean checkAccess, final TrackerOperation op )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        SetSshKeyTask setSshKeyTask = new SetSshKeyTask( environment, networkManager, resultHolder, sshKey, op );

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
    public void removeEnvironment( final UUID environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        removeEnvironment( environmentId, true );
    }


    public void removeEnvironment( final UUID environmentId, final boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        findEnvironment( environmentId, checkAccess );

        environmentDataService.remove( environmentId.toString() );

        notifyOnEnvironmentDestroyed( environmentId );
    }


    public void setupEnvironmentTunnel( final UUID environmentId, Set<Peer> peers ) throws EnvironmentTunnelException
    {
        String localEnvAlias =
                String.format( "env_%s_%s", peerManager.getLocalPeer().getId().toString(), environmentId.toString() );
        Set<Peer> remotePeers = Sets.newHashSet( peers );
        LocalPeer localPeer = peerManager.getLocalPeer();
        remotePeers.remove( localPeer );
        if ( !remotePeers.isEmpty() )
        {
            try
            {
                String localPeerCert = localPeer.exportEnvironmentCertificate( environmentId );

                for ( Peer remotePeer : remotePeers )
                {
                    String remotePeerCert = remotePeer.exportEnvironmentCertificate( environmentId );
                    String remoteEnvAlias =
                            String.format( "env_%s_%s", remotePeer.getId().toString(), environmentId.toString() );
                    localPeer.importCertificate( remotePeerCert, remoteEnvAlias );
                    remotePeer.importCertificate( localPeerCert, localEnvAlias );
                }
            }
            catch ( Exception e )
            {
                throw new EnvironmentTunnelException( "Error exchanging environment certificates ", e );
            }
        }
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


    public void setContainersTransientFields( final Set<ContainerHost> containers )
    {
        for ( ContainerHost containerHost : containers )
        {
            ( ( EnvironmentContainerImpl ) containerHost ).setDataService( environmentContainerDataService );
            ( ( EnvironmentContainerImpl ) containerHost ).setEnvironmentManager( this );
            ( ( EnvironmentContainerImpl ) containerHost ).setPeer( peerManager.getPeer( containerHost.getPeerId() ) );
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


    /** domain functions ** */

    @Override
    public void removeDomain( final UUID environmentId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Setting environment %s domain", environmentId ) );

        removeDomain( environmentId, op, async, true );
    }


    public void removeDomain( final UUID environmentId, final TrackerOperation op, final boolean async,
                              boolean checkAccess )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        SetDomainTask setDomainTask = new SetDomainTask( environment, resultHolder, op, null );

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
    public void assignDomain( final UUID environmentId, final String newDomain, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        TrackerOperation op = tracker.createTrackerOperation( TRACKER_SOURCE,
                String.format( "Assigning environment %s domain", environmentId ) );

        assignDomain( environmentId, op, newDomain, async, true );
    }


    public void assignDomain( final UUID environmentId, final TrackerOperation op, final String newDomain,
                              final boolean async, final boolean checkAccess )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newDomain ), "Invalid domain" );
        Preconditions.checkArgument( newDomain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId, checkAccess );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        SetDomainTask setDomainTask = new SetDomainTask( environment, resultHolder, op, newDomain );

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
    public String getDomain( final UUID environmentId ) throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        return getDomain( environmentId, true );
    }


    public String getDomain( final UUID environmentId, final boolean checkAccess )
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


    /** domain functions ** */


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


    public void notifyOnContainerDestroyed( final Environment environment, final UUID containerId )
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


    public void notifyOnEnvironmentDestroyed( final UUID environmentId )
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
                                   final Tracker tracker )
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
}

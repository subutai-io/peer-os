package org.safehaus.subutai.core.env.impl;


import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.environment.Blueprint;
import org.safehaus.subutai.common.environment.ContainerHostNotFoundException;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentModificationException;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.env.api.EnvironmentEventListener;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.env.impl.builder.TopologyBuilder;
import org.safehaus.subutai.core.env.impl.dao.BlueprintDataService;
import org.safehaus.subutai.core.env.impl.dao.EnvironmentContainerDataService;
import org.safehaus.subutai.core.env.impl.dao.EnvironmentDataService;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.safehaus.subutai.core.env.impl.tasks.CreateEnvironmentTask;
import org.safehaus.subutai.core.env.impl.tasks.DestroyEnvironmentTask;
import org.safehaus.subutai.core.env.impl.tasks.GrowEnvironmentTask;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class.getName() );

    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    private final TopologyBuilder topologyBuilder;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final String defaultDomain;

    private final Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();

    //************* DaoManager ******************
    private final DaoManager daoManager;

    //************* Data Managers ******************
    private EnvironmentDataService environmentDataService;
    private EnvironmentContainerDataService environmentContainerDataService;
    private BlueprintDataService blueprintDataService;


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                   final NetworkManager networkManager, final DaoManager daoManager,
                                   final String defaultDomain )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( defaultDomain ) );

        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.daoManager = daoManager;
        this.defaultDomain = defaultDomain;
        this.topologyBuilder = new TopologyBuilder( templateRegistry, peerManager, defaultDomain );
    }


    public void init() throws SQLException
    {
        this.blueprintDataService = new BlueprintDataService( daoManager );
        this.environmentDataService = new EnvironmentDataService( daoManager );
        this.environmentContainerDataService = new EnvironmentContainerDataService( daoManager );
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

        //set environment's transient fields
        setEnvironmentTransientFields( environment );

        //set container's transient fields
        setContainersTransientFields( environment.getContainerHosts() );

        return environment;
    }


    @Override
    public Environment createEnvironment( final String name, final Topology topology, boolean async )
            throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = new EnvironmentImpl( name );

        final ResultHolder<EnvironmentCreationException> resultHolder = new ResultHolder<>();

        CreateEnvironmentTask createEnvironmentTask =
                new CreateEnvironmentTask( this, environment, resultHolder, topology );

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

        return environment;
    }


    public void saveEnvironment( EnvironmentImpl environment )
    {
        environmentDataService.persist( environment );
    }


    public void build( EnvironmentImpl environment, Topology topology ) throws EnvironmentBuildException
    {
        topologyBuilder.build( environment, topology );
    }


    @Override
    public void destroyEnvironment( final UUID environmentId, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            throw new EnvironmentDestructionException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final ResultHolder<EnvironmentDestructionException> resultHolder = new ResultHolder<>();

        final Set<EnvironmentDestructionException> exceptions = Sets.newHashSet();

        DestroyEnvironmentTask destroyEnvironmentTask =
                new DestroyEnvironmentTask( this, environment, exceptions, resultHolder, forceMetadataRemoval );

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
        else
        {
            if ( !exceptions.isEmpty() )
            {
                LOG.error( String.format( "There were errors while destroying environment: %s", exceptions ) );
            }
        }
    }


    @Override
    public Set<ContainerHost> growEnvironment( final UUID environmentId, final Topology topology, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final Set<ContainerHost> newContainers = Sets.newHashSet();

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        GrowEnvironmentTask growEnvironmentTask =
                new GrowEnvironmentTask( this, environment, topology, resultHolder, newContainers );

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
    public void destroyContainer( final ContainerHost containerHost, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        destroyCont( containerHost, async, forceMetadataRemoval );
    }


    private void destroyCont( final ContainerHost containerHost, boolean async, final boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( containerHost, "Invalid container host" );

        final EnvironmentImpl environment =
                ( EnvironmentImpl ) findEnvironment( UUID.fromString( containerHost.getEnvironmentId() ) );


        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        try
        {
            environment.getContainerHostById( containerHost.getId() );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new EnvironmentModificationException( e );
        }

        final Semaphore semaphore = new Semaphore( 0 );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        executor.submit( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

                    try
                    {
                        try
                        {
                            ( ( EnvironmentContainerImpl ) containerHost ).destroy();
                        }
                        catch ( PeerException e )
                        {
                            if ( forceMetadataRemoval )
                            {
                                resultHolder.setResult( new EnvironmentModificationException( e ) );
                            }
                            else
                            {
                                throw e;
                            }
                        }

                        environment.removeContainer( containerHost.getId() );

                        notifyOnContainerDestroyed( environment, containerHost.getId() );
                    }
                    catch ( PeerException e )
                    {

                        environment.setStatus( EnvironmentStatus.UNHEALTHY );

                        throw new EnvironmentModificationException( e );
                    }

                    if ( environment.getContainerHosts().isEmpty() )
                    {
                        try
                        {
                            removeEnvironment( environment.getId() );
                        }
                        catch ( EnvironmentNotFoundException e )
                        {
                            LOG.error( "Error removing environment", e );
                        }
                    }
                    else
                    {
                        environment.setStatus( EnvironmentStatus.HEALTHY );
                    }
                }
                catch ( EnvironmentModificationException e )
                {
                    LOG.error( String.format( "Error destroying container %s", containerHost.getHostname() ), e );
                    resultHolder.setResult( e );
                }
                finally
                {
                    semaphore.release();
                }
            }
        } );

        if ( !async )
        {
            try
            {
                semaphore.acquire();

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
        findEnvironment( environmentId );

        environmentDataService.remove( environmentId.toString() );

        notifyOnEnvironmentDestroyed( environmentId );
    }


    public void setEnvironmentTransientFields( Environment environment )
    {
        ( ( EnvironmentImpl ) environment ).setDataService( environmentDataService );
        ( ( EnvironmentImpl ) environment ).setEnvironmentManager( this );
    }


    public void setContainersTransientFields( Set<ContainerHost> containers )
    {
        for ( ContainerHost containerHost : containers )
        {
            ( ( EnvironmentContainerImpl ) containerHost ).setDataService( environmentContainerDataService );
            ( ( EnvironmentContainerImpl ) containerHost ).setEnvironmentManager( this );
            ( ( EnvironmentContainerImpl ) containerHost ).setPeer( peerManager.getPeer( containerHost.getPeerId() ) );
        }
    }


    public void configureSsh( Set<ContainerHost> containerHosts ) throws NetworkManagerException
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


    public void configureHosts( Set<ContainerHost> containerHosts ) throws NetworkManagerException
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
    public void setSshKey( final UUID environmentId, final String sshKey, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) findEnvironment( environmentId );

        final ResultHolder<EnvironmentModificationException> resultHolder = new ResultHolder<>();

        final Semaphore semaphore = new Semaphore( 0 );

        executor.submit( new Runnable()
        {
            @Override
            public void run()
            {
                environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

                String oldSshKey = environment.getSshKey();

                environment.saveSshKey( sshKey );

                try
                {
                    if ( Strings.isNullOrEmpty( sshKey ) && !Strings.isNullOrEmpty( oldSshKey ) )
                    {
                        //remove old key from containers
                        networkManager.removeSshKeyFromAuthorizedKeys( environment.getContainerHosts(), oldSshKey );
                    }
                    else if ( !Strings.isNullOrEmpty( sshKey ) && Strings.isNullOrEmpty( oldSshKey ) )
                    {
                        //insert new key to containers
                        networkManager.addSshKeyToAuthorizedKeys( environment.getContainerHosts(), sshKey );
                    }
                    else if ( !Strings.isNullOrEmpty( sshKey ) && !Strings.isNullOrEmpty( oldSshKey ) )
                    {
                        //replace old ssh key with new one
                        networkManager
                                .replaceSshKeyInAuthorizedKeys( environment.getContainerHosts(), oldSshKey, sshKey );
                    }

                    environment.setStatus( EnvironmentStatus.HEALTHY );
                }
                catch ( NetworkManagerException e )
                {
                    LOG.error( String.format( "Error setting ssh key to environment %s", environment.getName() ), e );
                    environment.setStatus( EnvironmentStatus.UNHEALTHY );
                    resultHolder.setResult( new EnvironmentModificationException( e ) );
                }
                finally
                {
                    semaphore.release();
                }
            }
        } );


        if ( !async )
        {
            try
            {
                semaphore.acquire();

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
    public String getDefaultDomainName()
    {
        return defaultDomain;
    }


    public void registerListener( EnvironmentEventListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void unregisterListener( EnvironmentEventListener listener )
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
}

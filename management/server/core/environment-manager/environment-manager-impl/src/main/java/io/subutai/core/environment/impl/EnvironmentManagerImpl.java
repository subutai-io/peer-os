package io.subutai.core.environment.impl;


import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentSecurityException;
import io.subutai.core.environment.impl.dao.BlueprintDataService;
import io.subutai.core.environment.impl.dao.EnvironmentContainerDataService;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.tracker.api.Tracker;


public class EnvironmentManagerImpl implements EnvironmentManager
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class );

    private static final String TRACKER_SOURCE = "Environment Manager";

    private final IdentityManager identityManager;
    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    private final Tracker tracker;
    private final SecurityManager securityManager;
    private final TemplateRegistry templateRegistry;

    private final DaoManager daoManager;
    private final String defaultDomain;

    protected Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();

    protected EnvironmentDataService environmentDataService;
    protected EnvironmentContainerDataService environmentContainerDataService;
    protected BlueprintDataService blueprintDataService;

    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


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
        EnvironmentCreationWorkflow environmentCreationWorkflow =
                getEnvironmentCreationWorkflow( environment, topology, sshKey, operationTracker );

        //start environment creation workflow
        environmentCreationWorkflow.start();

        //notify environment event listeners
        environmentCreationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    notifyOnEnvironmentCreated( findEnvironment( environment.getId() ) );
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
            environmentCreationWorkflow.join();

            if ( environmentCreationWorkflow.getError() != null )
            {
                throw new EnvironmentCreationException(
                        exceptionUtil.getRootCause( environmentCreationWorkflow.getError() ) );
            }
        }

        //return created environment
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
    public Set<ContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                               final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        return null;
    }


    @Override
    public void setSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {

    }


    @Override
    public void destroyContainer( final String environmentId, final String containerId, final boolean async,
                                  final boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

    }


    @Override
    public Environment findEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        return findEnvironment( environmentId, false );
    }


    @Override
    public String getDefaultDomainName()
    {
        return defaultDomain;
    }


    //************ utility methods


    protected EnvironmentCreationWorkflow getEnvironmentCreationWorkflow( final EnvironmentImpl environment,
                                                                          final Topology topology, final String sshKey,
                                                                          final TrackerOperation operationTracker )
    {
        return new EnvironmentCreationWorkflow( defaultDomain, templateRegistry, networkManager, peerManager,
                environment, topology, sshKey, operationTracker );
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
        return getUser().isAdmin();
    }


    public void setEnvironmentTransientFields( final Environment environment )
    {
        ( ( EnvironmentImpl ) environment ).setDataService( environmentDataService );
        ( ( EnvironmentImpl ) environment ).setEnvironmentManager( this );
    }


    public void setContainersTransientFields( final Environment environment )
    {
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            ( ( EnvironmentContainerImpl ) containerHost ).setDataService( environmentContainerDataService );
            ( ( EnvironmentContainerImpl ) containerHost ).setEnvironmentManager( this );


            String peerId = containerHost.getPeerId();
            Peer peer = peerManager.getPeer( peerId );

            ( ( EnvironmentContainerImpl ) containerHost ).setPeer( peer );
        }
    }


    public void saveEnvironment( final EnvironmentImpl environment )
    {
        environmentDataService.persist( environment );
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


    protected EnvironmentImpl createEmptyEnvironment( final String name, final String subnetCidr, final String sshKey )
    {

        final EnvironmentImpl environment = new EnvironmentImpl( name, subnetCidr, sshKey, getUserId() );

        saveEnvironment( environment );

        setEnvironmentTransientFields( environment );

        notifyOnEnvironmentCreated( environment );

        return environment;
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


    protected Long getUserId()
    {
        return getUser().getId();
    }


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                   final NetworkManager networkManager, final DaoManager daoManager,
                                   final String defaultDomain, final IdentityManager identityManager,
                                   final Tracker tracker,
                                   final io.subutai.core.security.api.SecurityManager securityManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( defaultDomain ) );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( tracker );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.daoManager = daoManager;
        this.defaultDomain = defaultDomain;
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
}

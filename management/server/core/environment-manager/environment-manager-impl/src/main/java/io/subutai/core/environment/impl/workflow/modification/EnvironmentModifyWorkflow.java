package io.subutai.core.environment.impl.workflow.modification;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.ContainerCloneStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PrepareTemplatesStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.modification.steps.DestroyContainersStep;
import io.subutai.core.environment.impl.workflow.modification.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.modification.steps.ReservationStep;
import io.subutai.core.environment.impl.workflow.modification.steps.SetupP2PStep;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

//TODO
// 1 ) parallelize change container size step -extract a separate step
// 2 ) skip destroy containers & change quota steps if not needed
public class EnvironmentModifyWorkflow extends CancellableWorkflow<EnvironmentModifyWorkflow.EnvironmentGrowingPhase>
{
    private final PeerManager peerManager;
    private EnvironmentImpl environment;
    private final Topology topology;
    private List<String> removedContainers;
    private Map<String, ContainerSize> changedContainers;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;
    private final SecurityManager securityManager;


    //environment creation phases
    public enum EnvironmentGrowingPhase
    {
        INIT,
        MODIFY_CONTAINERS_QUOTA,
        DESTROY_CONTAINERS,
        GENERATE_KEYS,
        RESERVE_NET,
        SETUP_P2P,
        PREPARE_TEMPLATES,
        CLONE_CONTAINERS,
        CONFIGURE_HOSTS,
        CONFIGURE_SSH,
        FINALIZE

    }


    public EnvironmentModifyWorkflow( String defaultDomain, PeerManager peerManager, SecurityManager securityManager,
                                      EnvironmentImpl environment, Topology topology, List<String> removedContainers,
                                      Map<String, ContainerSize> changedContainers, TrackerOperation operationTracker,
                                      EnvironmentManagerImpl environmentManager )
    {

        super( EnvironmentGrowingPhase.INIT );

        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.environment = environment;
        this.topology = topology;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
        this.environmentManager = environmentManager;
        this.removedContainers = new ArrayList<>();
        this.removedContainers = removedContainers;
        this.changedContainers = new HashMap<>();
        this.changedContainers = changedContainers;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentGrowingPhase INIT()
    {
        operationTracker.addLog( "Initializing environment growth" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return changedContainers == null ? EnvironmentGrowingPhase.DESTROY_CONTAINERS :
               EnvironmentGrowingPhase.MODIFY_CONTAINERS_QUOTA;
    }


    public EnvironmentGrowingPhase MODIFY_CONTAINERS_QUOTA()
    {
        operationTracker.addLog( "Changing quota sizes" );

        try
        {
            for ( Map.Entry<String, ContainerSize> entry : changedContainers.entrySet() )
            {
                environment.getContainerHostById( entry.getKey() ).setContainerSize( entry.getValue() );
            }

            environment = ( EnvironmentImpl ) environmentManager.loadEnvironment( environment.getId() );

            return EnvironmentGrowingPhase.DESTROY_CONTAINERS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
        }

        return null;
    }


    public EnvironmentGrowingPhase DESTROY_CONTAINERS()
    {
        operationTracker.addLog( "Destroying containers" );

        try
        {
            boolean deleteOnly = topology == null || CollectionUtil.isCollectionEmpty( topology.getAllPeers() );

            if ( deleteOnly && environment.getContainerHosts().size() <= removedContainers.size() )
            {
                throw new IllegalStateException(
                        "Environment will have 0 containers after modification. Please, destroy environment instead" );
            }

            environment =
                    ( EnvironmentImpl ) new DestroyContainersStep( environment, environmentManager, removedContainers,
                            operationTracker ).execute();

            saveEnvironment();

            return deleteOnly ? EnvironmentGrowingPhase.FINALIZE : EnvironmentGrowingPhase.GENERATE_KEYS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Securing channel" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager, securityManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.RESERVE_NET;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase RESERVE_NET()
    {
        operationTracker.addLog( "Reserving network resources" );

        try
        {
            new ReservationStep( topology, environment, peerManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.SETUP_P2P;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase SETUP_P2P()
    {
        operationTracker.addLog( "Setting up networking" );

        try
        {
            new SetupP2PStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.PREPARE_TEMPLATES;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase PREPARE_TEMPLATES()
    {
        operationTracker.addLog( "Preparing templates" );

        try
        {
            new PrepareTemplatesStep( environment, peerManager, topology, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.CLONE_CONTAINERS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentGrowingPhase CLONE_CONTAINERS()
    {
        operationTracker.addLog( "Cloning containers" );

        try
        {
            new ContainerCloneStep( defaultDomain, topology, environment, peerManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring hosts" );

        try
        {
            new RegisterHostsStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            new RegisterSshStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentGrowingPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public void FINALIZE()
    {
        environment.setStatus( EnvironmentStatus.HEALTHY );

        saveEnvironment();

        operationTracker.addLogDone( "Environment is grown" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );

        saveEnvironment();

        operationTracker.addLogFailed( message );

        super.fail( message, e );
    }


    @Override
    public void onCancellation()
    {
        environment.setStatus( EnvironmentStatus.CANCELLED );

        saveEnvironment();

        operationTracker.addLogFailed( "Environment modification was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}

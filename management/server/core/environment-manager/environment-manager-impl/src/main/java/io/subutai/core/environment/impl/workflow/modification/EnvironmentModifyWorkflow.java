package io.subutai.core.environment.impl.workflow.modification;


import java.util.ArrayList;
import java.util.List;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.tracker.TrackerOperation;
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


public class EnvironmentModifyWorkflow extends Workflow<EnvironmentModifyWorkflow.EnvironmentGrowingPhase>
{
    private final PeerManager peerManager;
    private EnvironmentImpl environment;
    private final Topology topology;
    private List<String> removedContainers;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;
    private final SecurityManager securityManager;


    //environment creation phases
    public static enum EnvironmentGrowingPhase
    {
        INIT,
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
                                      TrackerOperation operationTracker, EnvironmentManagerImpl environmentManager )
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
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentGrowingPhase INIT()
    {
        operationTracker.addLog( "Initializing environment growth" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return EnvironmentGrowingPhase.DESTROY_CONTAINERS;
    }


    public EnvironmentGrowingPhase DESTROY_CONTAINERS()
    {
        operationTracker.addLog( "Removing containers" );

        try
        {
            new DestroyContainersStep( environment, environmentManager, removedContainers, operationTracker ).execute();

            saveEnvironment();

            if ( topology == null )
            {
                return EnvironmentGrowingPhase.FINALIZE;
            }

            return EnvironmentGrowingPhase.GENERATE_KEYS;
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
        operationTracker.addLog( "Cloning containers" );

        try
        {
            new PrepareTemplatesStep( peerManager, topology, operationTracker ).execute();

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
            new ContainerCloneStep( defaultDomain, topology, environment, peerManager, environmentManager,
                    operationTracker ).execute();

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
        operationTracker.addLog( "Configuring /etc/hosts" );

        try
        {
            new RegisterHostsStep( environment, operationTracker ).execute();

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
            new RegisterSshStep( environment, operationTracker ).execute();

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


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}

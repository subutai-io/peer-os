package io.subutai.core.environment.impl.workflow.creation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.ContainerCloneStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PrepareTemplatesStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.creation.steps.ReservationStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetupP2PStep;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


public class EnvironmentCreationWorkflow extends Workflow<EnvironmentCreationWorkflow.EnvironmentCreationPhase>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentCreationWorkflow.class );

    private final PeerManager peerManager;
    private final SecurityManager securityManager;
    private EnvironmentImpl environment;
    private final Topology topology;
    private final String sshKey;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    //environment creation phases
    public enum EnvironmentCreationPhase
    {
        INIT,
        GENERATE_KEYS,
        RESERVE_NET,
        SETUP_P2P,
        PREPARE_TEMPLATES,
        CLONE_CONTAINERS,
        CONFIGURE_HOSTS,
        CONFIGURE_SSH,
        FINALIZE

    }


    public EnvironmentCreationWorkflow( String defaultDomain, EnvironmentManagerImpl environmentManager,
                                        PeerManager peerManager, SecurityManager securityManager,
                                        EnvironmentImpl environment, Topology topology, String sshKey,
                                        TrackerOperation operationTracker )
    {
        super( EnvironmentCreationPhase.INIT );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.environment = environment;
        this.topology = topology;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentCreationPhase INIT()
    {
        operationTracker.addLog( "Initializing environment creation" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );
        environment = saveEnvironment();
        return EnvironmentCreationPhase.GENERATE_KEYS;
    }


    public EnvironmentCreationPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Securing channel" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager, securityManager, operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.RESERVE_NET;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentCreationPhase RESERVE_NET()
    {
        operationTracker.addLog( "Reserving network resources" );

        try
        {
            new ReservationStep( topology, environment, peerManager, operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.SETUP_P2P;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentCreationPhase SETUP_P2P()
    {
        operationTracker.addLog( "Setting up networking" );

        try
        {
            new SetupP2PStep( topology, environment, peerManager, operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.PREPARE_TEMPLATES;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentCreationPhase PREPARE_TEMPLATES()
    {
        operationTracker.addLog( "Preparing templates" );

        try
        {
            new PrepareTemplatesStep( peerManager, topology, operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.CLONE_CONTAINERS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentCreationPhase CLONE_CONTAINERS()
    {
        operationTracker.addLog( "Cloning containers" );

        try
        {
            new ContainerCloneStep( defaultDomain, topology, environment, peerManager, environmentManager,
                    operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentCreationPhase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring /etc/hosts" );

        try
        {
            new RegisterHostsStep( environment, operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentCreationPhase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            environment.addSshKey( sshKey );

            new RegisterSshStep( environment, operationTracker ).execute();

            environment = saveEnvironment();

            return EnvironmentCreationPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public void FINALIZE()
    {
        //        LOG.info( "Finalizing environment creation" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        environment = saveEnvironment();

        operationTracker.addLogDone( "Environment is created" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        super.fail( message, e );
        environment.setStatus( EnvironmentStatus.UNHEALTHY );
        operationTracker.addLogFailed( getFailedReason() );
        saveEnvironment();
    }


    protected EnvironmentImpl saveEnvironment()
    {
        LOGGER.debug( "Saving environment..." );
        try
        {
            environment = environmentManager.update( environment );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
        }
        return environment;
    }
}

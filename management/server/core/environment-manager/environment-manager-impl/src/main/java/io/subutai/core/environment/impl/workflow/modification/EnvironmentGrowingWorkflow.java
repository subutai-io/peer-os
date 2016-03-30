package io.subutai.core.environment.impl.workflow.modification;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.subutai.core.environment.impl.workflow.modification.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.modification.steps.SetupP2PStep;
import io.subutai.core.environment.impl.workflow.modification.steps.VNISetupStep;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentGrowingWorkflow extends Workflow<EnvironmentGrowingWorkflow.EnvironmentGrowingPhase>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentGrowingWorkflow.class );

    private final TemplateManager templateRegistry;
    private final PeerManager peerManager;
    private EnvironmentImpl environment;
    private final Topology topology;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    //environment creation phases
    public static enum EnvironmentGrowingPhase
    {
        INIT,
        GENERATE_KEYS,
        SETUP_VNI,
        SETUP_P2P,
        PREPARE_TEMPLATES,
        CLONE_CONTAINERS,
        CONFIGURE_HOSTS,
        CONFIGURE_SSH,
        FINALIZE

    }


    public EnvironmentGrowingWorkflow( String defaultDomain, TemplateManager templateRegistry, PeerManager peerManager,
                                       EnvironmentImpl environment, Topology topology,
                                       TrackerOperation operationTracker, EnvironmentManagerImpl environmentManager )
    {
        super( EnvironmentGrowingPhase.INIT );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.environment = environment;
        this.topology = topology;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
        this.environmentManager = environmentManager;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentGrowingPhase INIT()
    {
        operationTracker.addLog( "Initializing environment growth" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        environment = environmentManager.update( environment );

        return EnvironmentGrowingPhase.GENERATE_KEYS;
    }


    public EnvironmentGrowingPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Generating PEKs" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager, operationTracker ).execute();

            environment = environmentManager.update( environment );

            return EnvironmentGrowingPhase.SETUP_VNI;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentGrowingPhase SETUP_VNI()
    {
        operationTracker.addLog( "Setting up VNI" );

        try
        {
            new VNISetupStep( topology, environment, peerManager, operationTracker ).execute();

            environment = environmentManager.update( environment );

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
        operationTracker.addLog( "Setting up P2P" );

        try
        {
            new SetupP2PStep( topology, environment, peerManager, operationTracker ).execute();

            environment = environmentManager.update( environment );

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
            new PrepareTemplatesStep( peerManager, topology, operationTracker ).execute();

            environment = environmentManager.update( environment );

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

            environment = environmentManager.update( environment );

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

            environment = environmentManager.update( environment );

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

            environment = environmentManager.update( environment );

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
        LOG.info( "Finalizing environment growth" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        environment = environmentManager.update( environment );

        operationTracker.addLogDone( "Environment is grown" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        saveFailState();
        super.fail( message, e );
    }


    private void saveFailState()
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );
        environment = environmentManager.update( environment );
        operationTracker.addLogFailed( getFailedReason() );
        LOG.error( "Error growing environment", getFailedException() );
    }
}

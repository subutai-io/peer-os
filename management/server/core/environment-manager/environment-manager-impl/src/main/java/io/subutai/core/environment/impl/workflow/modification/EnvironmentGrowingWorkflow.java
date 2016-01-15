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
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetSshKeyStep;
import io.subutai.core.environment.impl.workflow.modification.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.modification.steps.SetupN2NStep;
import io.subutai.core.environment.impl.workflow.modification.steps.VNISetupStep;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentGrowingWorkflow extends Workflow<EnvironmentGrowingWorkflow.EnvironmentGrowingPhase>
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentGrowingWorkflow.class );

    private final TemplateManager templateRegistry;
    private final NetworkManager networkManager;
    private final PeerManager peerManager;
    private EnvironmentImpl environment;
    private final Topology topology;
    private final String sshKey;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;

    private Throwable error;


    //environment creation phases
    public static enum EnvironmentGrowingPhase
    {
        INIT,
        GENERATE_KEYS,
        SETUP_VNI,
        SETUP_N2N,
        CLONE_CONTAINERS,
        CONFIGURE_HOSTS,
        CONFIGURE_SSH,
        SET_ENVIRONMENT_SSH_KEY,
        FINALIZE

    }


    public EnvironmentGrowingWorkflow( String defaultDomain, TemplateManager templateRegistry,
                                       NetworkManager networkManager, PeerManager peerManager,
                                       EnvironmentImpl environment, Topology topology, String sshKey,
                                       TrackerOperation operationTracker, EnvironmentManagerImpl environmentManager )
    {
        super( EnvironmentGrowingPhase.INIT );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.environment = environment;
        this.topology = topology;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
        this.environmentManager = environmentManager;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentGrowingPhase INIT()
    {
        operationTracker.addLog( "Initializing environment growth" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        environment = environmentManager.saveOrUpdate( environment );

        return EnvironmentGrowingPhase.GENERATE_KEYS;
    }


    public EnvironmentGrowingPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Generating PEKs" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager.getLocalPeer() ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.SETUP_VNI;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentGrowingPhase SETUP_VNI()
    {
        operationTracker.addLog( "Setting up VNI" );

        try
        {
            new VNISetupStep( topology, environment, peerManager.getLocalPeer() ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.SETUP_N2N;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentGrowingPhase SETUP_N2N()
    {
        operationTracker.addLog( "Setting up N2N" );

        try
        {
            new SetupN2NStep( topology, environment ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.CLONE_CONTAINERS;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentGrowingPhase CLONE_CONTAINERS()
    {
        operationTracker.addLog( "Cloning containers" );

        try
        {
            new ContainerCloneStep( templateRegistry, defaultDomain, topology, environment, peerManager.getLocalPeer(),
                    environmentManager )
                    .execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentGrowingPhase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring /etc/hosts" );

        try
        {
            new RegisterHostsStep( environment, networkManager ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentGrowingPhase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            new RegisterSshStep( environment, networkManager ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.SET_ENVIRONMENT_SSH_KEY;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentGrowingPhase SET_ENVIRONMENT_SSH_KEY()
    {
        operationTracker.addLog( "Setting environment ssh key to containers" );

        try
        {
            new SetSshKeyStep( sshKey, environment, networkManager ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return EnvironmentGrowingPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public void FINALIZE()
    {
        LOG.info( "Finalizing environment growth" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        environment = environmentManager.saveOrUpdate( environment );

        operationTracker.addLogDone( "Environment is grown" );

        //this is a must have call
        stop();
    }


    public Throwable getError()
    {
        return error;
    }


    public void setError( final Throwable error )
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );
        environment = environmentManager.saveOrUpdate( environment );

        this.error = error;
        LOG.error( "Error growing environment", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }
}

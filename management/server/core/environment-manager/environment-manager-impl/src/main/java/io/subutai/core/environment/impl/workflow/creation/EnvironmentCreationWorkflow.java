package io.subutai.core.environment.impl.workflow.creation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.ContainerCloneStep;
import io.subutai.core.environment.impl.workflow.creation.steps.N2NSetupStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetSshKeyStep;
import io.subutai.core.environment.impl.workflow.creation.steps.VNISetupStep;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;


public class EnvironmentCreationWorkflow extends Workflow<EnvironmentCreationWorkflow.EnvironmentCreationPhase>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentCreationWorkflow.class );

    private final TemplateRegistry templateRegistry;
    private final NetworkManager networkManager;
    private final PeerManager peerManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final String sshKey;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;

    private Throwable error;


    //environment creation phases
    public static enum EnvironmentCreationPhase
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


    public EnvironmentCreationWorkflow( String defaultDomain, TemplateRegistry templateRegistry,
                                        NetworkManager networkManager, PeerManager peerManager,
                                        EnvironmentImpl environment, Topology topology, String sshKey,
                                        TrackerOperation operationTracker )
    {
        super( EnvironmentCreationPhase.INIT );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
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

        return EnvironmentCreationPhase.GENERATE_KEYS;
    }


    public EnvironmentCreationPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Generating PEKs" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager.getLocalPeer() ).execute();

            return EnvironmentCreationPhase.SETUP_VNI;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentCreationPhase SETUP_VNI()
    {
        operationTracker.addLog( "Setting up VNI" );

        try
        {
            new VNISetupStep( topology, environment, peerManager.getLocalPeer() ).execute();

            return EnvironmentCreationPhase.SETUP_N2N;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentCreationPhase SETUP_N2N()
    {
        operationTracker.addLog( "Setting up N2N" );

        try
        {
            new N2NSetupStep( topology, environment, peerManager.getLocalPeer().getPeerInfo().getIp(),
                    Common.SUPER_NODE_PORT, peerManager.getLocalPeer() ).execute();

            return EnvironmentCreationPhase.CLONE_CONTAINERS;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentCreationPhase CLONE_CONTAINERS()
    {
        operationTracker.addLog( "Cloning containers" );

        try
        {
            new ContainerCloneStep( templateRegistry, defaultDomain, topology, environment, peerManager.getLocalPeer() )
                    .execute();

            return EnvironmentCreationPhase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentCreationPhase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring /etc/hosts" );

        try
        {
            new RegisterHostsStep( environment, networkManager ).execute();

            return EnvironmentCreationPhase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentCreationPhase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            new RegisterSshStep( environment, networkManager ).execute();

            return EnvironmentCreationPhase.SET_ENVIRONMENT_SSH_KEY;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentCreationPhase SET_ENVIRONMENT_SSH_KEY()
    {
        operationTracker.addLog( "Setting environment ssh key to containers" );

        try
        {
            new SetSshKeyStep( sshKey, environment, networkManager ).execute();

            return EnvironmentCreationPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public void FINALIZE()
    {
        LOG.info( "Finalizing environment creation" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        operationTracker.addLogDone( "Environment is created" );

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
        this.error = error;
        LOG.error( "Error creating environment", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }
}

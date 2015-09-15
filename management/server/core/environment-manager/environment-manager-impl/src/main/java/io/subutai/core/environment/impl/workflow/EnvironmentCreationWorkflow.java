package io.subutai.core.environment.impl.workflow;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.Topology;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.step.ContainerCloneStep;
import io.subutai.core.environment.impl.workflow.step.N2NSetupStep;
import io.subutai.core.environment.impl.workflow.step.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.step.VNISetupStep;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentCreationWorkflow extends Workflow<EnvironmentCreationWorkflow.EnvironmentCreationPhase>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentCreationWorkflow.class );

    private final PeerManager peerManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final String subnetCidr;
    private final String sshKey;
    private final TrackerOperation operationTracker;

    private Throwable error;


    public Throwable getError()
    {
        return error;
    }


    public void setError( final Throwable error )
    {
        this.error = error;
        LOG.error( "Error creating environment", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }


    //environment creation phases
    public static enum EnvironmentCreationPhase
    {
        INIT,
        GENERATE_KEYS,
        SETUP_N2N,
        SETUP_VNI,
        CLONE_CONTAINERS,
        FINALIZE

    }


    public EnvironmentCreationWorkflow( PeerManager peerManager, EnvironmentImpl environment, Topology topology,
                                        String subnetCidr, String sshKey, TrackerOperation operationTracker )
    {
        super( EnvironmentCreationPhase.INIT );

        this.peerManager = peerManager;
        this.environment = environment;
        this.topology = topology;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.subnetCidr = subnetCidr;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentCreationPhase INIT()
    {
        operationTracker.addLog( "Initializing environment creation" );

        return EnvironmentCreationPhase.GENERATE_KEYS;
    }


    public EnvironmentCreationPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Generating PEKs" );

        try
        {
            new PEKGenerationStep().execute( topology, environment );

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
            new N2NSetupStep().execute();

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
            new VNISetupStep().execute( topology, environment );

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
            new ContainerCloneStep().execute( topology, environment );

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

        //this is a must have call
        stop();
    }
}

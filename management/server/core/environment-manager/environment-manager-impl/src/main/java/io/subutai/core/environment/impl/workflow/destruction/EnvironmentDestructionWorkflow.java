package io.subutai.core.environment.impl.workflow.destruction;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.destruction.steps.CleanUpNetworkStep;
import io.subutai.core.environment.impl.workflow.destruction.steps.CleanupN2NStep;
import io.subutai.core.environment.impl.workflow.destruction.steps.DestroyContainersStep;
import io.subutai.core.environment.impl.workflow.destruction.steps.RemoveKeysStep;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentDestructionWorkflow extends Workflow<EnvironmentDestructionWorkflow.EnvironmentDestructionPhase>
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentDestructionWorkflow.class );

    private final PeerManager peerManager;
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final TrackerOperation operationTracker;
    final boolean forceMetadataRemoval;

    private Throwable error;


    public Throwable getError()
    {
        return error;
    }


    public void setError( final Throwable error )
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );
        this.error = error;
        LOG.error( "Error growing environment", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }


    public EnvironmentDestructionWorkflow( final PeerManager peerManager,
                                           final EnvironmentManagerImpl environmentManager,
                                           final EnvironmentImpl environment, final TrackerOperation operationTracker,
                                           final boolean forceMetadataRemoval )
    {
        super( EnvironmentDestructionPhase.INIT );

        this.peerManager = peerManager;
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.operationTracker = operationTracker;
        this.forceMetadataRemoval = forceMetadataRemoval;
    }


    public static enum EnvironmentDestructionPhase
    {
        INIT,
        DESTROY_CONTAINERS,
        CLEANUP_NETWORKING,
        CLEANUP_N2N,
        REMOVE_KEYS,
        FINALIZE
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentDestructionPhase INIT()
    {
        operationTracker.addLog( "Initializing environment destruction" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        return EnvironmentDestructionPhase.DESTROY_CONTAINERS;
    }


    public EnvironmentDestructionPhase DESTROY_CONTAINERS()
    {
        operationTracker.addLog( "Destroying containers" );

        try
        {
            new DestroyContainersStep( environment, environmentManager, forceMetadataRemoval ).execute();

            return EnvironmentDestructionPhase.CLEANUP_NETWORKING;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentDestructionPhase CLEANUP_NETWORKING()
    {
        operationTracker.addLog( "Cleaning up networking" );

        try
        {
            new CleanUpNetworkStep( environment, peerManager.getLocalPeer() ).execute();

            return EnvironmentDestructionPhase.CLEANUP_N2N;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentDestructionPhase CLEANUP_N2N()
    {
        operationTracker.addLog( "Cleaning up N2N" );

        try
        {
            new CleanupN2NStep( environment, peerManager.getLocalPeer() ).execute();

            return EnvironmentDestructionPhase.REMOVE_KEYS;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public EnvironmentDestructionPhase REMOVE_KEYS()
    {
        operationTracker.addLog( "Removing keys" );

        try
        {
            new RemoveKeysStep( environment, peerManager.getLocalPeer() ).execute();

            return EnvironmentDestructionPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public void FINALIZE()
    {
        LOG.info( "Finalizing environment destruction" );

        operationTracker.addLogDone( "Environment is destroyed" );

        //this is a must have call
        stop();
    }
}

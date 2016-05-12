package io.subutai.core.environment.impl.workflow.destruction;


import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.destruction.steps.CleanupEnvironmentStep;


public class EnvironmentDestructionWorkflow
        extends CancellableWorkflow<EnvironmentDestructionWorkflow.EnvironmentDestructionPhase>
{
    private final EnvironmentManagerImpl environmentManager;
    private EnvironmentImpl environment;
    private final TrackerOperation operationTracker;


    public enum EnvironmentDestructionPhase
    {
        INIT,
        CLEANUP_ENVIRONMENT,
        FINALIZE
    }


    public EnvironmentDestructionWorkflow( final EnvironmentManagerImpl environmentManager,
                                           final EnvironmentImpl environment, final TrackerOperation operationTracker )
    {
        super( EnvironmentDestructionPhase.INIT );

        this.environmentManager = environmentManager;
        this.environment = environment;
        this.operationTracker = operationTracker;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentDestructionPhase INIT()
    {
        operationTracker.addLog( "Initializing environment destruction" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return EnvironmentDestructionPhase.CLEANUP_ENVIRONMENT;
    }


    public EnvironmentDestructionPhase CLEANUP_ENVIRONMENT()
    {
        operationTracker.addLog( "Destroying environment" );

        try
        {
            new CleanupEnvironmentStep( environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentDestructionPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public void FINALIZE()
    {

        environmentManager.remove( environment );

        RelationManager relationManager = environmentManager.getRelationManager();
        relationManager.removeRelation( environment );

        operationTracker.addLogDone( "Environment is destroyed" );

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

        operationTracker.addLogFailed( "Environment destruction was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}

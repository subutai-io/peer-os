package io.subutai.core.environment.impl.workflow.destruction;


import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.destruction.steps.DestroyContainerStep;


public class ContainerDestructionWorkflow
        extends CancellableWorkflow<ContainerDestructionWorkflow.ContainerDestructionPhase>
{
    private final EnvironmentManagerImpl environmentManager;
    private EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final TrackerOperation operationTracker;


    public enum ContainerDestructionPhase
    {
        INIT, VALIDATE, DESTROY_CONTAINER, FINALIZE
    }


    public ContainerDestructionWorkflow( final EnvironmentManagerImpl environmentManager,
                                         final EnvironmentImpl environment, final ContainerHost containerHost,
                                         final TrackerOperation operationTracker )
    {
        super( ContainerDestructionPhase.INIT );

        this.environmentManager = environmentManager;
        this.environment = environment;
        this.containerHost = containerHost;
        this.operationTracker = operationTracker;
    }


    //********************* WORKFLOW STEPS ************


    public ContainerDestructionPhase INIT()
    {
        operationTracker.addLog( "Initializing container destruction" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return ContainerDestructionPhase.VALIDATE;
    }


    public ContainerDestructionPhase VALIDATE()
    {
        operationTracker.addLog( "Validating environment state" );

        if ( environment.getContainerHosts().size() <= 1 )
        {
            fail( "Environment has 0 or 1 container. Please, destroy environment instead" );

            return null;
        }
        else
        {
            return ContainerDestructionPhase.DESTROY_CONTAINER;
        }
    }


    public ContainerDestructionPhase DESTROY_CONTAINER()
    {
        operationTracker.addLog( "Destroying container" );

        try
        {
            new DestroyContainerStep( environmentManager, environment, containerHost ).execute();

            RelationManager relationManager = environmentManager.getRelationManager();
            relationManager.removeRelation( containerHost );

            saveEnvironment();

            return ContainerDestructionPhase.FINALIZE;
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

        operationTracker.addLogDone( "Container is destroyed" );

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

        operationTracker.addLogFailed( "Container destruction was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}

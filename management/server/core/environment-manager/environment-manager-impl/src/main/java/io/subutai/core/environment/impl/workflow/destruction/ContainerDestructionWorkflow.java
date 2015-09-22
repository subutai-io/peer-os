package io.subutai.core.environment.impl.workflow.destruction;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.destruction.steps.DestroyContainerStep;


public class ContainerDestructionWorkflow extends Workflow<ContainerDestructionWorkflow.ContainerDestructionPhase>
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerDestructionWorkflow.class );

    private final EnvironmentManager environmentManager;
    private final EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final boolean forceMetadataRemoval;
    private final TrackerOperation operationTracker;

    private Throwable error;

    private boolean skippedDestruction;


    public static enum ContainerDestructionPhase
    {
        INIT, VALIDATE, DESTROY_CONTAINER, FINALIZE
    }


    public ContainerDestructionWorkflow( final EnvironmentManager environmentManager,
                                         final EnvironmentImpl environment, final ContainerHost containerHost,
                                         final boolean forceMetadataRemoval, final TrackerOperation operationTracker )
    {
        super( ContainerDestructionPhase.INIT );

        this.environmentManager = environmentManager;
        this.environment = environment;
        this.containerHost = containerHost;
        this.forceMetadataRemoval = forceMetadataRemoval;
        this.operationTracker = operationTracker;
    }


    //********************* WORKFLOW STEPS ************


    public ContainerDestructionPhase INIT()
    {
        operationTracker.addLog( "Initializing container destruction" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        return ContainerDestructionPhase.VALIDATE;
    }


    public ContainerDestructionPhase VALIDATE()
    {
        operationTracker.addLog( "Validating environment state" );

        if ( environment.getContainerHosts().size() <= 1 )
        {
            operationTracker.addLog( "Environment has 0 or 1 container. Please, destroy environment instead" );

            skippedDestruction = true;

            return ContainerDestructionPhase.FINALIZE;
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
            new DestroyContainerStep( environmentManager, environment, containerHost, forceMetadataRemoval,
                    operationTracker ).execute();

            return ContainerDestructionPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public void FINALIZE()
    {
        LOG.info( "Finalizing container destruction" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        operationTracker.addLogDone( skippedDestruction ? "Container is not destroyed" : "Container is destroyed" );

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
        LOG.error( "Error destroying container", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }
}

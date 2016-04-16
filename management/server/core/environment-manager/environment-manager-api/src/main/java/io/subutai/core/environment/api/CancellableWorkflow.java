package io.subutai.core.environment.api;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.servicemix.beanflow.Workflow;


public abstract class CancellableWorkflow<T> extends Workflow<T>
{
    private final ExecutorService executor;


    private CancellableWorkflow( final T firstStep, ExecutorService executor )
    {
        super( executor = getExecutor(), firstStep );

        this.executor = executor;
    }


    private static ExecutorService getExecutor()
    {
        return Executors.newSingleThreadExecutor();
    }


    public CancellableWorkflow( final T firstStep )
    {
        this( firstStep, null );
    }


    /**
     * Cancels the ongoing environment workflow.
     *
     * After this call the environment state becomes unpredictable.
     *
     * Please use this only if you want to destroy the environment afterwards.
     */
    public void cancel()
    {
        //stop workflow
        this.stop();

        //interrupt main thread
        this.executor.shutdownNow();

        //call cancellation handler
        onCancellation();
    }


    public abstract void onCancellation();
}

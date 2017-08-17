package io.subutai.core.environment.api;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.servicemix.beanflow.Workflow;


public abstract class CancellableWorkflow<T> extends Workflow<T>
{
    private final ExecutorService executor;

    private Throwable error;


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

        try
        {
            this.executor.awaitTermination( 5, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
        }
        finally
        {
            //call cancellation handler
            onCancellation();
        }
    }


    public abstract void onCancellation();


    public Throwable getError()
    {
        return error;
    }


    public void fail( String message, Throwable e )
    {
        this.error = e;
        super.fail( message, e );
    }
}

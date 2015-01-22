package org.safehaus.subutai.core.environment.ui.executor.build;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


public class BuildProcessExecutorImpl implements BuildProcessExecutor
{
    private Set<BuildProcessExecutionListener> listeners = new HashSet<>();

    private EnvironmentBuildProcess buildProcess;


    public BuildProcessExecutorImpl( final EnvironmentBuildProcess buildProcess )
    {
        this.buildProcess = buildProcess;
    }


    @Override
    public void addListener( final BuildProcessExecutionListener listener )
    {
        this.listeners.add( listener );
    }


    @Override
    public void execute( final ExecutorService executor, final BuildProcessCommandFactory commandFactory )
    {
        final CompletionService<BuildProcessExecutionEvent> completionService =
                new ExecutorCompletionService( executor );

        completionService.submit( new Callable()
        {
            public BuildProcessExecutionEvent call()
            {
                fireEvent( new BuildProcessExecutionEvent( buildProcess, BuildProcessExecutionEventType.START,
                        "Started" ) );
                try
                {
                    BuildProcessCommand command = commandFactory.newCommand();
                    command.execute();
                    return ( new BuildProcessExecutionEvent( buildProcess, BuildProcessExecutionEventType.SUCCESS,
                            "Success" ) );
                }
                catch ( BuildProcessExecutionException ce )
                {
                    return ( new BuildProcessExecutionEvent( buildProcess, BuildProcessExecutionEventType.FAIL,
                            ce.getMessage() ) );
                }
            }
        } );


        ExecutorService waiter = Executors.newCachedThreadPool();
        waiter.execute( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Future<BuildProcessExecutionEvent> future = completionService.take();
                    BuildProcessExecutionEvent result = future.get();
                    fireEvent( result );
                }
                catch ( InterruptedException | ExecutionException e )
                {
                    fireEvent( new BuildProcessExecutionEvent( buildProcess, BuildProcessExecutionEventType.FAIL,
                            e.getMessage() ) );
                }
            }
        } );
        waiter.shutdown();
    }


    private void fireEvent( BuildProcessExecutionEvent event )
    {
        for ( BuildProcessExecutionListener listener : listeners )
        {
            if ( listener != null )
            {
                listener.onExecutionEvent( event );
            }
        }
    }
}

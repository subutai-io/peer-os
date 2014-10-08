package org.safehaus.subutai.core.environment.ui.executor.build;


import java.util.concurrent.ExecutorService;


/**
 * Created by bahadyr on 9/23/14.
 */
public interface BuildProcessExecutor
{

    void addListener( BuildProcessExecutionListener listener );


    void execute( ExecutorService executor, BuildProcessCommandFactory commandFactory );
}

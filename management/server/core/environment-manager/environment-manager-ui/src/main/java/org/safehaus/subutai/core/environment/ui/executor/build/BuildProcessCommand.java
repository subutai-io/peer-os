package org.safehaus.subutai.core.environment.ui.executor.build;


/**
 * Created by bahadyr on 9/23/14.
 */
public interface BuildProcessCommand
{
    public void execute() throws BuildProcessExecutionException;
}

package org.safehaus.subutai.core.environment.ui.executor;


import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by timur on 9/8/14.
 */
public class BuildCommand implements BuildProcessCommand
{

    private EnvironmentManager environmentManager;
    private EnvironmentBuildProcess environmentBuildProcess;


    public BuildCommand( EnvironmentManager environmentManager, EnvironmentBuildProcess environmentBuildProcess )
    {
        this.environmentManager = environmentManager;
        this.environmentBuildProcess = environmentBuildProcess;
    }


    @Override
    public void execute() throws BuildProcessExecutionException
    {
        try
        {
            environmentManager.buildEnvironment( environmentBuildProcess );
        }
        catch ( EnvironmentBuildException e )
        {
            throw new BuildProcessExecutionException( e.getMessage() );
        }
    }
}

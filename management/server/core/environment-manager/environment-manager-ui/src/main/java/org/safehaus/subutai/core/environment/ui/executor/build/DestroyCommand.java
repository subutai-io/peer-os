package org.safehaus.subutai.core.environment.ui.executor.build;


import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by timur on 9/8/14.
 */
public class DestroyCommand implements BuildProcessCommand
{
    private static final Logger LOG = LoggerFactory.getLogger( DestroyCommand.class.getName() );
    private EnvironmentManager environmentManager;
    private EnvironmentBuildProcess environmentBuildProcess;


    public DestroyCommand( EnvironmentManager environmentManager, EnvironmentBuildProcess environmentBuildProcess )
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
            LOG.error( e.getMessage(), e );
            throw new BuildProcessExecutionException( e.getMessage() );
        }
    }
}

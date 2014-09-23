package org.safehaus.subutai.core.environment.ui.executor;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by timur on 9/8/14.
 */
public class CloneCommand implements BuildProcessCommand
{
    private EnvironmentManager environmentManager;
    private EnvironmentBuildProcess environmentBuildProcess;


    public CloneCommand( EnvironmentManager environmentManager, EnvironmentBuildProcess environmentBuildProcess )
    {
        this.environmentManager = environmentManager;
        this.environmentBuildProcess = environmentBuildProcess;
    }


    @Override
    public void execute() throws BuildProcessExecutionException
    {
            environmentManager.buildEnvironment( environmentBuildProcess );

        /*}
        catch ( ContainerCreateException e )
        {
            throw new BuildProcessExecutionException( e.getMessage() );
        }*/
    }
}

package org.safehaus.subutai.core.environment.ui.executor;


import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by timur on 9/8/14.
 */
public class CloneCommandFactory implements BuildProcessCommandFactory
{
    private EnvironmentManager environmentManager;
    private EnvironmentBuildProcess environmentBuildProcess;


    public CloneCommandFactory( final EnvironmentManager environmentManager,
                                final EnvironmentBuildProcess environmentBuildProcess )
    {
        this.environmentManager = environmentManager;
        this.environmentBuildProcess = environmentBuildProcess;
    }


    @Override
    public BuildProcessCommand newCommand()
    {
        return new CloneCommand( environmentManager, environmentBuildProcess );
    }
}

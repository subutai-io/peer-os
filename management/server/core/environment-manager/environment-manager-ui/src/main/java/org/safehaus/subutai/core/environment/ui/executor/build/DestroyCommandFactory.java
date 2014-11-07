package org.safehaus.subutai.core.environment.ui.executor.build;


import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by timur on 9/8/14.
 */
public class DestroyCommandFactory implements BuildProcessCommandFactory
{
    private EnvironmentManager environmentManager;
    private EnvironmentBuildProcess environmentBuildProcess;


    public DestroyCommandFactory( final EnvironmentManager environmentManager,
                                  final EnvironmentBuildProcess environmentBuildProcess )
    {
        this.environmentManager = environmentManager;
        this.environmentBuildProcess = environmentBuildProcess;
    }


    @Override
    public BuildProcessCommand newCommand()
    {
        return new BuildCommand( environmentManager, environmentBuildProcess );
    }
}

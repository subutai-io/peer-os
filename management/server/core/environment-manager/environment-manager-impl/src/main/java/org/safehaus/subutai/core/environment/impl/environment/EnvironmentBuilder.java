package org.safehaus.subutai.core.environment.impl.environment;


import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface EnvironmentBuilder
{

    public Environment build( final EnvironmentBlueprint blueprint, final EnvironmentBuildProcess process )
            throws BuildException;
}

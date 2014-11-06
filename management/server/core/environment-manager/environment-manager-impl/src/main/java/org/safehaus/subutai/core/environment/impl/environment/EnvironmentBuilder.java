package org.safehaus.subutai.core.environment.impl.environment;


import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface EnvironmentBuilder
{

    public void build(final EnvironmentBuildProcess process) throws BuildException;

}

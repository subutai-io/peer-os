package org.safehaus.subutai.core.environment.impl.environment;


import org.safehaus.subutai.core.environment.api.helper.Environment;


/**
 * Created by bahadyr on 11/7/14.
 */
public interface EnvironmentDestroyer
{

    public void destroy( Environment environment ) throws DestroyException;
}

package org.safehaus.subutai.core.environment.api;


import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Container;


/**
 * Created by timur on 9/22/14.
 */
public class EnvironmentContainer extends Container {

    private EnvironmentManager environmentManager;


    public void setEnvironmentManager( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    public boolean start() throws ContainerException
    {
        return environmentManager.startContainer( this );
    }


    @Override
    public boolean stop() throws ContainerException
    {
        return environmentManager.stopContainer( this );
    }


    @Override
    public boolean isConnected() throws ContainerException
    {
        return environmentManager.isContainerConnected( this );
    }
}

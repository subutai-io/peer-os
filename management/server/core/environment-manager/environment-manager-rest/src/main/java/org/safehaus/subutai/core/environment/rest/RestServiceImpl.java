package org.safehaus.subutai.core.environment.rest;


import org.safehaus.subutai.core.environment.api.EnvironmentManager;


public class RestServiceImpl implements RestService
{

    private EnvironmentManager environmentManager;


    public RestServiceImpl()
    {
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    public String buildNodeGroup( final String peer )
    {
        return null;
    }
}
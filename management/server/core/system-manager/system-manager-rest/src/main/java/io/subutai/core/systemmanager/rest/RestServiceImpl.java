package io.subutai.core.systemmanager.rest;


import javax.ws.rs.core.Response;

import io.subutai.core.systemmanager.api.SystemManager;


/**
 * Created by ermek on 2/5/16.
 */
public class RestServiceImpl implements RestService
{
    private SystemManager systemManager;

    @Override
    public Response getSubutaiInfo()
    {
        return null;
    }


    public void setSystemManager( final SystemManager systemManager )
    {
        this.systemManager = systemManager;
    }
}

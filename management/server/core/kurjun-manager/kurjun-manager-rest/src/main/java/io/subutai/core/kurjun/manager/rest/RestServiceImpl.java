package io.subutai.core.kurjun.manager.rest;


import javax.ws.rs.core.Response;

import io.subutai.core.kurjun.manager.api.KurjunManager;


public class RestServiceImpl implements RestService
{
    private KurjunManager kurjunManager;


    public Response getAuthId()
    {
        return null;
    }


    public void setKurjunManager( final KurjunManager kurjunManager )
    {
        this.kurjunManager = kurjunManager;
    }
}

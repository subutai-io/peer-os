package org.safehaus.subuta.pet.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.pet.api.PetManager;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService
{

    private static final String MSG_RESPONSE = "MSG_RESPONSE";
    private PetManager petManager;


    public RestServiceImpl()
    {
    }


    public PetManager getPetManager()
    {
        return petManager;
    }


    public void setPetManager( final PetManager petManager )
    {
        this.petManager = petManager;
    }


    @Override
    public String welcome( String name )
    {

        String hello = petManager.helloPet( name );
        return JsonUtil.toJson( MSG_RESPONSE, hello );
    }


    @Override
    public void somePost()
    {

    }
}
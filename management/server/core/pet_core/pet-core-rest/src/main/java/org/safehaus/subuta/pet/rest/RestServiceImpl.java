package org.safehaus.subuta.pet.rest;


import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.pet.api.PetManager;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private PetManager petManager;
    private static final String MSG_RESPONSE = "MSG_RESPONSE";


    public PetManager getPetManager() {
        return petManager;
    }


    public void setPetManager( final PetManager petManager ) {
        this.petManager = petManager;
    }


    public RestServiceImpl() {
    }


    @Override
    public String welcome( String name ) {

        String hello = petManager.helloPet( name );
        return JsonUtil.toJson( MSG_RESPONSE, hello );
    }
}
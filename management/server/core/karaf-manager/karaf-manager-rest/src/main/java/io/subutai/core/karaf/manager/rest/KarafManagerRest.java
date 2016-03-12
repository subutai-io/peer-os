package io.subutai.core.karaf.manager.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Rest for KarafManagerImpl
 */
public interface KarafManagerRest
{
    /********************************
     *
     */
    @GET
    @Path( "cmd" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response runCommand( @QueryParam( "command" ) String command);

}

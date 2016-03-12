package io.subutai.core.karaf.manager.rest;


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
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addKey( @FormParam( "command" ) String command);

}

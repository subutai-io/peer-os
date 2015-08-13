package io.subutai.core.keyserver.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Interface for Key Server REST
 */
public interface KeyServerRest
{

    /********************************
     *
     */
    @POST
    @Path( "savekey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response saveSecurityKey( @FormParam( "keyId" ) String keyId,
                                     @FormParam( "fingerprint" ) String fingerprint,
                                     @FormParam( "keyType" ) short keyType,
                                     @FormParam( "keyData" ) String keyData);


    /********************************
     *
     */
    @GET
    @Path( "getkey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSecurityKey( @QueryParam( "keyId" ) String keyId );


    /********************************
     *
     */
    @GET
    @Path( "getkeys" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSecurityKeys();


    /********************************
     *
     */
    @DELETE
    @Path( "removekey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response removeSecurityKey( @QueryParam( "keyId" ) String keyId );

}

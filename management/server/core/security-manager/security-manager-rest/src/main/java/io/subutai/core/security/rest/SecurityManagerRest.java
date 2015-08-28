package io.subutai.core.security.rest;


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
public interface SecurityManagerRest
{

    /* *******************************
     *
     */
    @POST
    @Path( "keyman/addpublickeyring" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addPublicKeyRing( @FormParam( "hostid" ) String hostId, @FormParam( "keytext" ) String keyText );


    /* *******************************
     *
     */
    @DELETE
    @Path( "keyman/removepublickeyring" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response removePublicKeyRing( @QueryParam( "hostid" ) String hostId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickeyring" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKeyRing( @QueryParam( "hostid" ) String hostId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickeyid" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKeyId( @QueryParam ( "hostid" ) String   hostId);

    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickeyfingerprint" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKeyFingerprint( @QueryParam ( "hostid" ) String   hostId);

}

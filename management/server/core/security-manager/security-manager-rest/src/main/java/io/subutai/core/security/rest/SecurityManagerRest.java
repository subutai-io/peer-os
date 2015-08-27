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
    @Path( "keyman/addpublickey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addPublicKey( @FormParam( "hostid" ) String hostId, @FormParam( "keytext" ) String keyText );

    /* *******************************
     *
     */
    @POST
    @Path( "keyman/addsecuritykey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addSecurityKey( @FormParam( "hostid" ) String hostId, @FormParam( "keytext" ) String keyText,
                                    @FormParam( "keytype" ) short keyType );

    /* *******************************
     *
     */
    @DELETE
    @Path( "keyman/removepublickey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response removePublicKey( @QueryParam( "hostid" ) String hostId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickey" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKey( @QueryParam( "hostid" ) String hostId );


}

package io.subutai.core.security.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.core.security.rest.model.SecurityKeyData;


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
    public Response addPublicKeyRing( @FormParam( "hostid" ) String identityId, @FormParam( "keytext" ) String keyText );


    /* *******************************
     *
     */
    @DELETE
    @Path( "keyman/removepublickeyring" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response removePublicKeyRing( @QueryParam( "hostid" ) String identityId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickeyring" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKeyRing( @QueryParam( "hostid" ) String identityId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickeyid" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKeyId( @QueryParam( "hostid" ) String identityId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/getpublickeyfingerprint" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKeyFingerprint( @QueryParam( "hostid" ) String identityId );


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/trust/tree/user" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getUserKeyTrustTree();


    /* *******************************
     *
     */
    @GET
    @Path( "keyman/trust/tree" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getKeyTrustTree( @QueryParam( "identityid" ) String identityId );



    /* *******************************
     *
     */
    @PUT
    @Path( "keyman/trust/revoke" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response revokeKey( @QueryParam( "source" ) String source, @QueryParam( "target" ) String target );



    /* *******************************
     *
     */
    @PUT
    @Path( "keyman/trust" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response setTrust( @QueryParam( "source" ) String source, @QueryParam( "target" ) String target,
                              @QueryParam( "level" ) int trustLevel );



    /* *******************************
     *
     */
    @GET
    @Path( "keyman/trust/verify" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response verifyTrust( @QueryParam( "source" ) String source, @QueryParam( "target" ) String target);



    /* *******************************
     *
     */
    @GET
    @Path( "keyman/signature/verify" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response verifySignature( @QueryParam( "source" ) String source, @QueryParam( "target" ) String target);


    /* *******************************
     *
     */
    @PUT
    @Path( "keyman/trust/allow" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response allowKey( @QueryParam( "source" ) String source, @QueryParam( "target" ) String target );


    /* *******************************
     *
     */
    @PUT
    @Path( "keyman/signature" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response signKey( SecurityKeyData keyData );
}

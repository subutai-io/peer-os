package io.subutai.core.script.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


/**
 * Interface for Script Manager REST
 */
public interface ScriptManagerRest
{
    @POST
    @Path( "/" )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response uploadScript( @Multipart( "script" ) Attachment scriptFile );

    @DELETE
    @Path( "{scriptName}" )
    public Response removeScript( @PathParam( "scriptName" ) String scriptName );

    @GET
    @Path( "{scriptName}" )
    @Produces( { MediaType.APPLICATION_OCTET_STREAM } )
    public Response downloadScript( @PathParam( "scriptName" ) String scriptName );


    @GET
    @Path( "/" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listScripts();
}

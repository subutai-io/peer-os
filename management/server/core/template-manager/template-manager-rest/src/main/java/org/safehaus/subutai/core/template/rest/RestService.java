package org.safehaus.subutai.core.template.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


public interface RestService
{

    @GET
    @Path( "management_hostname" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String getManagementHostName();

    @PUT
    @Path( "management_hostname/{hostname}" )
    public void setManagementHostName( @PathParam( "hostname" ) String hostname );

    @POST
    @Path( "/" )
    @Consumes( { MediaType.MULTIPART_FORM_DATA } )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response importTemplate( @Multipart( "file" ) Attachment in, @Multipart( "config_dir" ) String configDir );

    @GET
    @Path( "{template}" )
    @Produces( { MediaType.APPLICATION_OCTET_STREAM } )
    public Response exportTemplate( @PathParam( "template" ) String templateName );

    @DELETE
    @Path( "{template}" )
    public Response unregister( @PathParam( "template" ) String templateName );
}

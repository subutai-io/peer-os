package io.subutai.core.pluginmanager.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


public interface RestService
{
    // register plugin
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    @Path( "upload" )
    javax.ws.rs.core.Response uploadPlugin( @Multipart( "name" ) String name, @Multipart( "version" ) String version,
                                            @Multipart( value = "kar" ) Attachment kar,
                                            @Multipart( "permission" ) String permissionJson );

    // get installed plugins list
    @GET
    @Path( "plugins/registered" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installedPlugins();


    // get plugin details
    @GET
    @Path( "plugins/registered/{pluginId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPluginDetails( @PathParam( "pluginId" ) String pluginId );


    // delete profile
    @DELETE
    @Path( "plugins/{userId}" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response deleteProfile( @PathParam( "userId" ) String pluginId );


    // set permission for plugin
    @POST
    @Path( "plugins/permission" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPermissions( @FormParam( "pluginId" ) String pluginId,
                                    @FormParam( "permission" ) String permissionJson );
}

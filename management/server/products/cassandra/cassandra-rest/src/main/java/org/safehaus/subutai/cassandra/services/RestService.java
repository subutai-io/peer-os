package org.safehaus.subutai.cassandra.services;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path( "cassandra" )
public interface RestService {

    @GET
    @Path( "install/{clusterName}/{domainName}/{numberOfNodes}/{numberOfSeeds}" )
    public String install( @PathParam( "clusterName" ) String clusterName, @PathParam( "domainName" ) String domainName,
                           @PathParam( "numberOfNodes" ) String numberOfNodes,
                           @PathParam( "numberOfSeeds" ) String numberOfSeeds );

    @GET
    @Path( "uninstall/{clusterName}" )
    public String uninstall( @PathParam( "clusterName" ) String clusterName );

    @POST
    @Path("install_from_json")
    @Consumes( MediaType.APPLICATION_JSON )
    public Response installFromJson( final String json );
}
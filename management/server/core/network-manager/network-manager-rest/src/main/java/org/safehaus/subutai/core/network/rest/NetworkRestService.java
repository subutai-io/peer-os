package org.safehaus.subutai.core.network.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * REST services related to networking setup of peers.
 *
 */
public interface NetworkRestService
{

    @GET
    @Path( "n2n" )
    @Produces( MediaType.APPLICATION_JSON )
    Response listN2NConnections();


    @POST
    @Path( "n2n" )
    Response setupN2NConnection( @FormParam( "n2n" ) String n2n, @FormParam( "keyFile" ) String keyFilePath );


    @GET
    @Path( "tunnel" )
    Response listTunnels();


    @POST
    @Path( "tunnel" )
    Response setupTunnel( @FormParam( "tunnel" ) String tunnel );


    @POST
    @Path( "containers/{name}/ip" )
    Response setContainerIp( @PathParam( "name" ) String containerName, @FormParam( "ip" ) String ip,
                             @FormParam( "netMask" ) int netMask, @FormParam( "vLanId" ) int vLanId );


    @POST
    @Path( "gateway" )
    Response setupGateway( @FormParam( "gatewayIp" ) String gatewayIp, @FormParam( "vLanId" ) int vLanId );


    @DELETE
    @Path( "gateway" )
    Response removeGateway( @FormParam( "vLanId" ) int vLanId );


    @POST
    @Path( "containers/{name}/gateway" )
    Response setupGatewayOnContainer( @PathParam( "name" ) String containerName,
                                      @FormParam( "gatewayIp" ) String gatewayIp,
                                      @FormParam( "interfaceName" ) String interfaceName );


    @DELETE
    @Path( "containers/{name}/gateway" )
    Response removeGatewayOnContainer( @PathParam( "name" ) String containerName );


    @POST
    @Path( "mapping" )
    Response setupVniVLanMapping( @FormParam( "tunnelName" ) String tunnelName,
                                  @FormParam( "vni" ) int vni,
                                  @FormParam( "vLanId" ) int vLanId );


    @DELETE
    @Path( "mapping" )
    Response removeVniVLanMapping( @FormParam( "tunnelName" ) String tunnelName,
                                   @FormParam( "vni" ) int vni,
                                   @FormParam( "vLanId" ) int vLanId );

}


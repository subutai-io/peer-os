package io.subutai.core.systemmanager.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;


public interface RestService
{
    @GET
    @Path( "about" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getSubutaiInfo() throws ConfigurationException;

    @GET
    @Path( "peer_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getPeerSettings();

    @POST
    @Path( "update_peer_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setPeerSettings();


    @GET
    @Path( "peer_policy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getPeerPolicy();

    @POST
    @Path( "update_peer_policy" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setPeerPolicy( @FormParam( "peerId" ) String peerId, @FormParam( "diskUsageLimit" ) String diskUsageLimit,
                            @FormParam( "cpuUsageLimit" ) String cpuUsageLimit,
                            @FormParam( "memoryUsageLimit" ) String memoryUsageLimit,
                            @FormParam( "environmentLimit" ) String environmentLimit,
                            @FormParam( "containerLimit" ) String containerLimit );


    @GET
    @Path( "network_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getNetworkSettings() throws ConfigurationException;

    @POST
    @Path( "update_network_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setNetworkSettings( @FormParam( "publicUrl" ) String publicUrl,
                                 @FormParam( "publicSecurePort" ) String publicSecurePort,
                                 @FormParam( "useRhIp" ) boolean userRhIp ) throws ConfigurationException;

    @GET
    @Path( "advanced_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getAdvancedSettings( @QueryParam( "logfile" ) String logFile );


    @GET
    @Path( "management_updates" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getManagementUpdates();


    @POST
    @Path( "update_management" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response update();

    @GET
    @Path( "updates_history" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getUpdatesHistory();

    @GET
    @Path( "is_update_in_progress" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response isUpdateInProgress();

    @GET
    @Path( "is_env_workflow_in_progress" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response isEnvironmentWorkflowInProgress();

    @GET
    @Path( "bazaar_ip" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response getBazaarIp();
}

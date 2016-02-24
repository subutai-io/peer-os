package io.subutai.core.systemmanager.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;


/**
 * Created by ermek on 2/6/16.
 */
public interface RestService
{
    @GET
    @Path( "about" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSubutaiInfo() throws ConfigurationException;

    @GET
    @Path( "peer_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerSettings();

    @POST
    @Path( "update_peer_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPeerSettings();


    @POST
    @Path( "update_registration_status" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setRegistrationStatus( @FormParam( "status" ) String status );


    @GET
    @Path( "kurjun_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getKurjunSettings() throws ConfigurationException;


    @POST
    @Path( "update_kurjun_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setKurjunSettings( @FormParam( "globalKurjunUrls" ) String globalKurjunUrls,
                                       @FormParam( "publicDiskQuota" ) String publicDiskQuota,
                                       @FormParam( "publicThreshold" ) String publicThreshold,
                                       @FormParam( "publicTimeFrame" ) String publicTimeFrame,
                                       @FormParam( "trustDiskQuota" ) String trustDiskQuota,
                                       @FormParam( "trustThreshold" ) String trustThreshold,
                                       @FormParam( "trustTimeFrame" ) String trustTimeFrame )
            throws ConfigurationException;


    @GET
    @Path( "peer_policy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerPolicy();

    @POST
    @Path( "update_peer_policy" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPeerPolicy( @FormParam( "peerId" ) String peerId,
                                   @FormParam( "diskUsageLimit" ) String diskUsageLimit,
                                   @FormParam( "cpuUsageLimit" ) String cpuUsageLimit,
                                   @FormParam( "memoryUsageLimit" ) String memoryUsageLimit,
                                   @FormParam( "environmentLimit" ) String environmentLimit,
                                   @FormParam( "containerLimit" ) String containerLimit );


    @GET
    @Path( "network_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getNetworkSettings() throws ConfigurationException;

    @POST
    @Path( "update_network_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setNetworkSettings( @FormParam( "securePortX1" ) String securePortX1,
                                        @FormParam( "securePortX2" ) String securePortX2,
                                        @FormParam( "securePortX3" ) String securePortX3 ) throws ConfigurationException;
}

package io.subutai.core.systemmanager.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by ermek on 2/6/16.
 */
public interface RestService
{
    @GET
    @Path( "about" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSubutaiInfo();

    @GET
    @Path( "peer_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerSettings();

    @POST
    @Path( "update_peer_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPeerSettings();

    @GET
    @Path( "kurjun_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getKurjunSettings();


    @POST
    @Path( "update_kurjun_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setKurjunSettings( @FormParam( "globalKurjunUrls" ) String globalKurjunUrls,
                                       @FormParam( "publicDiskQuota" ) String publicDiskQuota,
                                       @FormParam( "publicThreshold" ) String publicThreshold,
                                       @FormParam( "publicTimeFrame" ) String publicTimeFrame,
                                       @FormParam( "trustDiskQuota" ) String trustDiskQuota,
                                       @FormParam( "trustThreshold" ) String trustThreshold,
                                       @FormParam( "trustTimeFrame" ) String trustTimeFrame );


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
    public Response getNetworkSettings();

    @POST
    @Path( "update_network_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setNetworkSettings( @FormParam( "externalIpInterface" ) String externalIpInterface,
                                        @FormParam( "openPort" ) String openPort,
                                        @FormParam( "securePortX1" ) String securePortX1,
                                        @FormParam( "securePortX2" ) String securePortX2,
                                        @FormParam( "securePortX3" ) String securePortX3,
                                        @FormParam( "specialPortX1" ) String specialPortX1 );


    @GET
    @Path( "security_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSecuritySettings();


    @POST
    @Path( "update_security_settings" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setSecuritySettings( @FormParam( "encryptionEnabled" ) boolean encryptionEnabled,
                                         @FormParam( "restEncryptionEnabled" ) boolean restEncryptionEnabled,
                                         @FormParam( "integrationEnabled" ) boolean integrationEnabled,
                                         @FormParam( "keyTrustCheckEnabled" ) boolean keyTrustCheckEnabled );
}

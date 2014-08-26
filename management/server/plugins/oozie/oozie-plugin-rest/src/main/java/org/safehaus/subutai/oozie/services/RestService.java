package org.safehaus.subutai.oozie.services;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface RestService {

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path("install/{clusterName}/{serverHostname}/{hadoopClusterName}")
    public String installCluster( @PathParam("clusterName") String clusterName,
                                  @PathParam("serverHostname") String serverHostname,
                                  @PathParam("hadoopClusterName") String hadoopClusterName );

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "uninstall/{clusterName}" ) //Maps for the `hello/John` in the URL
    public String uninstallCluster( @PathParam( "clusterName" ) String clusterName );

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "startCluster/{clusterName}" ) //Maps for the `hello/John` in the URL
    public String startCluster( @PathParam( "clusterName" ) String clusterName );

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "stopCluster/{clusterName}" ) //Maps for the `hello/John` in the URL
    public String stopCluster( @PathParam( "clusterName" ) String clusterName );

    @GET

    @Produces( MediaType.APPLICATION_JSON )
    @Path("checkCluster/{clusterName}") //Maps for the `hello/John` in the URL
    public String checkCluster( @PathParam("clusterName") String clusterName );
}
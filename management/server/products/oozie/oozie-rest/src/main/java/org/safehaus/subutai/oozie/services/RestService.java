package org.safehaus.subutai.oozie.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("oozie")
public interface RestService {

    @GET
    @Path("install/{clusterName}")
    public String installCluster(@PathParam("clusterName") String clusterName,
                                       @PathParam("domainName") String domainInfo,
                                       @PathParam( "serverHostname" ) String serverHostname,
                                       @PathParam( "clientHostnames" )String... clientsHostnames);

    @GET
    @Path("uninstall/{clusterName}") //Maps for the `hello/John` in the URL
    public String uninstallCluster(@PathParam("clusterName") String clusterName);


}
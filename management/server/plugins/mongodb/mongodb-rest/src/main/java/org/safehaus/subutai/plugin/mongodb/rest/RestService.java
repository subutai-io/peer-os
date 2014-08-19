package org.safehaus.subutai.plugin.mongodb.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path ("mongodb")
public interface RestService {

	@GET
	@Path ("install/{clusterName}")
	public String installCluster(@PathParam ("clusterName") String clusterName);

	@GET
	@Path ("uninstall/{clusterName}") //Maps for the `hello/John` in the URL
	public String uninstallCluster(@PathParam ("clusterName") String clusterName);


}
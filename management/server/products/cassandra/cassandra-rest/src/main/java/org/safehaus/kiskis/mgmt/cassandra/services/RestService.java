package org.safehaus.kiskis.mgmt.cassandra.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("install")
public interface RestService {
    @GET
    @Path("cluster/{clusterName}/{domainName}/{numberOfNodes}/{numberOfSeeds}")
    public String handleGet(@PathParam("clusterName") String clusterName,
                            @PathParam("domainName") String domainName,
                            @PathParam("numberOfNodes") String numberOfNodes,
                            @PathParam("numberOfSeeds") String numberOfSeeds);
}
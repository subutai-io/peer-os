package org.safehaus.subutai.cassandra.services;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path ("cassandra")
public interface RestService {

	@GET
	@Path ("install/{clusterName}/{domainName}/{numberOfNodes}/{numberOfSeeds}")
	@Produces (MediaType.APPLICATION_JSON)
	public String install(@PathParam ("clusterName") String clusterName, @PathParam ("domainName") String domainName,
	                      @PathParam ("numberOfNodes") String numberOfNodes,
	                      @PathParam ("numberOfSeeds") String numberOfSeeds);

	@GET
	@Path ("uninstall/{clusterName}")
	@Produces (MediaType.APPLICATION_JSON)
	public String uninstall(@PathParam ("clusterName") String clusterName);

	//    @POST
	//    @Path("install_from_json")
	//    @Consumes(MediaType.APPLICATION_JSON)
	//    public Response installFromJson( final String json );

	@GET
	@Path ("startNode/{clusterName}/{lxchostname}")
	@Produces (MediaType.APPLICATION_JSON)
	public String startNode(@PathParam ("clusterName") String clusterName,
	                        @PathParam ("lxchostname") String lxchostname);

	@GET
	@Path ("stopNode/{clusterName}/{lxchostname}")
	@Produces (MediaType.APPLICATION_JSON)
	public String stopNode(@PathParam ("clusterName") String clusterName,
	                       @PathParam ("lxchostname") String lxchostname);

	@GET
	@Path ("checkNode/{clusterName}/{lxchostname}")
	@Produces (MediaType.APPLICATION_JSON)
	public String checkNode(@PathParam ("clusterName") String clusterName,
	                        @PathParam ("lxchostname") String lxchostname);
}
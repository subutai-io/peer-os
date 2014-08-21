package org.safehaus.subutai.accumulo.services;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


public interface RestService {

	@GET
	@Path ("list_clusters")
	@Produces ({MediaType.APPLICATION_JSON})
	public String listClusters();

	@GET
	@Path ("get_cluster/{clustername}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getCluster(@PathParam ("clustername") String source);

	@GET
	@Path ("destroy_cluster/{clustername}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String destroyCluster(@PathParam ("clustername") String clusterName);

	@GET
	@Path ("start_cluster/{clustername}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String startCluster(@PathParam ("clustername") String clusterName);

	@GET
	@Path ("stop_cluster/{clustername}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String stopCluster(@PathParam ("clustername") String clusterName);

	@GET
	@Path ("create_cluster")
	@Produces ({MediaType.APPLICATION_JSON})
	public String createCluster(@QueryParam ("config") String config);

	@GET
	@Path ("add_node/{clustername}/{lxchostname}/{nodetype}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String addNode(@PathParam ("clustername") String clustername, @PathParam ("lxchostname") String lxchostname,
	                      @PathParam ("nodetype") String nodetype);

	@GET
	@Path ("destroy_node/{clustername}/{lxchostname}/{nodetype}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String destroyNode(@PathParam ("clustername") String clustername,
	                          @PathParam ("lxchostname") String lxchostname, @PathParam ("nodetype") String nodetype);

	@GET
	@Path ("check_node/{clustername}/{lxchostname}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String checkNode(@PathParam ("clustername") String clustername,
	                        @PathParam ("lxchostname") String lxchostname);
}
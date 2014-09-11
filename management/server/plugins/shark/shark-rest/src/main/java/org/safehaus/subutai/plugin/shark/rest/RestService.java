package org.safehaus.subutai.plugin.shark.rest;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class RestService {
	private static final String OPERATION_ID = "OPERATION_ID";

	private Shark sharkManager;
	private Spark sparkManager;
	private AgentManager agentManager;

	public void setSharkManager(Shark sharkManager) {
		this.sharkManager = sharkManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setSparkManager(Spark sparkManager) {
		this.sparkManager = sparkManager;
	}

	@GET
    @Path("clusters")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response listClusters() {

		List<SharkClusterConfig > configList = sharkManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (SharkClusterConfig config : configList) {
			clusterNames.add(config.getClusterName());
		}

        String clusters = JsonUtil.GSON.toJson(clusterNames);
        return Response.status(Response.Status.OK).entity(clusters).build();
    }

	@GET
    @Path("clusters/{clusterName}")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response getCluster(
            @PathParam ("clusterName") String clusterName
	) {
        String cluster = JsonUtil.GSON.toJson(sharkManager.getCluster(clusterName));
        return Response.status(Response.Status.OK).entity(cluster).build();
    }

    @POST
    @Path("clusters")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response installCluster(@PathParam("clusterName") String clusterName) {
        SparkClusterConfig sparkConfig = sparkManager.getCluster(clusterName);
		SharkClusterConfig sharkConfig = new SharkClusterConfig();

		sharkConfig.setClusterName(sparkConfig.getClusterName());
		sharkConfig.setNodes(sparkConfig.getAllNodes());

        String operationId = JsonUtil.toJson(OPERATION_ID, sharkManager.installCluster(sharkConfig));
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }

    @DELETE
    @Path("clusters/{clusterName}")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response uninstallCluster(@PathParam("clusterName") String clusterName) {
        String operationId = JsonUtil.toJson(OPERATION_ID, sharkManager.uninstallCluster(clusterName));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }

    @POST
    @Path("clusters/{clusterName}/nodes/{lxcHostName}")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response addNode(
            @PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
        String operationId = JsonUtil.toJson(OPERATION_ID, sharkManager.addNode(clusterName, lxcHostName));
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }

    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxcHostName}")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response destroyNode(
            @PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
        String operationId = JsonUtil.toJson(OPERATION_ID, sharkManager.destroyNode(clusterName, lxcHostName));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }

    @PUT
    @Path ("actualize_master_ip/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
    public Response actualizeMasterIP(
            @PathParam ("clusterName") String clusterName) {
        String operationId = JsonUtil.toJson(OPERATION_ID, sharkManager.actualizeMasterIP(clusterName));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }
}
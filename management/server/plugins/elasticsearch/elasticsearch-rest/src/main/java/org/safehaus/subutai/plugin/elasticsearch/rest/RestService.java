package org.safehaus.subutai.plugin.elasticsearch.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";
    private Elasticsearch elasticsearch;
    private AgentManager agentManager;

    @GET
    @Path("clusters")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response listClusters() {

        List<ElasticsearchClusterConfiguration > elasticsearchClusterConfigurationList = elasticsearch.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for (ElasticsearchClusterConfiguration elasticsearchClusterConfiguration : elasticsearchClusterConfigurationList ) {
            clusterNames.add( elasticsearchClusterConfiguration.getClusterName());
        }

        String clusters = JsonUtil.GSON.toJson(clusterNames);
        return Response.status(Response.Status.OK).entity(clusters).build();
    }


	@POST
    @Path("clusters")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response installCluster(
            @PathParam("clusterName") String clusterName,
            @QueryParam("numberOfNodes") int numberOfNodes,
            @QueryParam("numberOfMasterNodes") int numberOfMasterNodes,
            @QueryParam("numberOfDataNodes") int numberOfDataNodes,
            @QueryParam("numberOfShards") int numberOfShards,
            @QueryParam("numberOfReplicas") int numberOfReplicas
    ) {

        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = new ElasticsearchClusterConfiguration();
        elasticsearchClusterConfiguration.setClusterName(clusterName);
        elasticsearchClusterConfiguration.setNumberOfNodes(numberOfNodes);
        elasticsearchClusterConfiguration.setNumberOfMasterNodes(numberOfMasterNodes);
        elasticsearchClusterConfiguration.setNumberOfDataNodes(numberOfDataNodes);
        elasticsearchClusterConfiguration.setNumberOfShards(numberOfShards);
        elasticsearchClusterConfiguration.setNumberOfReplicas(numberOfReplicas);

        UUID uuid = elasticsearch.installCluster( elasticsearchClusterConfiguration );

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @DELETE
    @Path("clusters/{clusterName}")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response uninstallCluster(
            @PathParam("clusterName") String clusterName
    ) {

        UUID uuid = elasticsearch.uninstallCluster(clusterName);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @GET
    @Path("clusters/{clusterName}/nodes")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response checkAllNodes(
            @PathParam("clusterName") String clusterName
    ) {

        UUID uuid = elasticsearch.checkAllNodes(clusterName);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @PUT
    @Path("clusters/{clusterName}/nodes/start")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response startAllNodes(
            @PathParam("clusterName") String clusterName
    ) {

        UUID uuid = elasticsearch.startAllNodes(clusterName);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @PUT
    @Path("clusters/{clusterName}/nodes/stop")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response stopAllNodes(
            @PathParam("clusterName") String clusterName
    ) {

        UUID uuid = elasticsearch.stopAllNodes(clusterName);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @POST
    @Path("clusters/{clusterName}/nodes/{node}")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response addNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = elasticsearch.addNode(clusterName, node);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @DELETE
    @Path("clusters/{clusterName}/nodes/{node}")
    @Produces ( {MediaType.APPLICATION_JSON})
    public Response destroyNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = elasticsearch.destroyNode(clusterName, node);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }
}
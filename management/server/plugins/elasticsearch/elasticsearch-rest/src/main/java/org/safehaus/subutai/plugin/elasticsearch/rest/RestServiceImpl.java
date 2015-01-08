package org.safehaus.subutai.plugin.elasticsearch.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RestServiceImpl implements RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";
    private Elasticsearch elasticsearch;


    public void setElasticsearch(final Elasticsearch elasticsearch)
    {
        this.elasticsearch = elasticsearch;
    }

    @Override
    public Response listClusters()
    {
        List<ElasticsearchClusterConfiguration> elasticsearchClusterConfigurationList = elasticsearch.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for (ElasticsearchClusterConfiguration elasticsearchClusterConfiguration :
                elasticsearchClusterConfigurationList)
        {
            clusterNames.add(elasticsearchClusterConfiguration.getClusterName());
        }

        String clusters = JsonUtil.GSON.toJson(clusterNames);
        return Response.status(Response.Status.OK).entity(clusters).build();
    }


    @Override
    public Response installCluster(String clusterName, int numberOfNodes)
    {

        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = new ElasticsearchClusterConfiguration();
        elasticsearchClusterConfiguration.setClusterName(clusterName);
        elasticsearchClusterConfiguration.setNumberOfNodes(numberOfNodes);

        UUID uuid = elasticsearch.installCluster(elasticsearchClusterConfiguration);

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response uninstallCluster(String clusterName)
    {
        UUID uuid = elasticsearch.uninstallCluster(clusterName);
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response checkAllNodes(String clusterName)
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster(clusterName);
        UUID uuid = elasticsearch.checkAllNodes(config);
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response startAllNodes(String clusterName)
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster(clusterName);
        UUID uuid = elasticsearch.startAllNodes(config);
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response stopAllNodes(String clusterName)
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster(clusterName);
        UUID uuid = elasticsearch.stopAllNodes(config);
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response addNode(String clusterName, String node)
    {
        UUID uuid = elasticsearch.addNode(clusterName, node);
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response destroyNode(String clusterName, String node)
    {
        UUID uuid = elasticsearch.destroyNode(clusterName, node);
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }
}
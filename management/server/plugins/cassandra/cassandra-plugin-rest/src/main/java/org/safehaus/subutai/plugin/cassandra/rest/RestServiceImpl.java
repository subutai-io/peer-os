package org.safehaus.subutai.plugin.cassandra.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.api.TrimmedCassandraClusterConfig;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 9/4/14.
 */
public class RestServiceImpl implements RestService {

    private Cassandra cassandraManager;


    public Cassandra getCassandraManager() {
        return cassandraManager;
    }


    public void setCassandraManager( final Cassandra cassandraManager ) {
        this.cassandraManager = cassandraManager;
    }


    @Override
    public Response listClusters() {
        List<CassandraClusterConfig> configs = cassandraManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( CassandraClusterConfig config : configs ) {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.toJson(clusterNames);
        return Response.status(Response.Status.CREATED).entity(clusters).build();
    }


    @Override
    public Response getCluster(final String source) {
        String cluster = JsonUtil.toJson(cassandraManager.getCluster(source));
        return Response.status(Response.Status.OK).entity(cluster).build();
    }


    @Override
    public Response createCluster(final String config) {
        TrimmedCassandraClusterConfig trimmedCassandraConfig =
                JsonUtil.fromJson( config, TrimmedCassandraClusterConfig.class );

        CassandraClusterConfig cassandraConfig = new CassandraClusterConfig();
        cassandraConfig.setClusterName( trimmedCassandraConfig.getClusterName() );
        cassandraConfig.setDomainName( trimmedCassandraConfig.getDomainName() );
        cassandraConfig.setNumberOfNodes( trimmedCassandraConfig.getNumberOfNodes() );
        cassandraConfig.setNumberOfSeeds( trimmedCassandraConfig.getNumberOfSeeds() );

        UUID uuid = cassandraManager.installCluster( cassandraConfig );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response destroyCluster(final String clusterName) {
        UUID uuid = cassandraManager.uninstallCluster( clusterName );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response startCluster(final String clusterName) {
        UUID uuid = cassandraManager.startCluster( clusterName );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response stopCluster(final String clusterName) {
        UUID uuid = cassandraManager.stopCluster( clusterName );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response addNode(final String clusterName, final String lxcHostname, final String nodeType) {
        UUID uuid = cassandraManager.addNode(clusterName, lxcHostname, nodeType);
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response destroyNode(final String clusterName, final String lxcHostname, final String nodeType) {
        UUID uuid = cassandraManager.destroyNode(clusterName, lxcHostname, nodeType);
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response checkNode(final String clusterName, final String lxcHostname) {
        UUID uuid = cassandraManager.checkNode(clusterName, lxcHostname);
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}

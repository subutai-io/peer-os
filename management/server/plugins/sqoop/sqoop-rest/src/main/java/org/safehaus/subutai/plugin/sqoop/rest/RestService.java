package org.safehaus.subutai.plugin.sqoop.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.sqoop.api.DataSourceType;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.setting.ExportSetting;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportParameter;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;

import org.apache.commons.lang.StringUtils;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Sqoop sqoopManager;

    public void setSqoopManager( Sqoop sqoopManager )
    {
        this.sqoopManager = sqoopManager;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getClusters()
    {

        List<SqoopConfig> configs = sqoopManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( SqoopConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @GET
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName )
    {
        SqoopConfig config = sqoopManager.getCluster( clusterName );

        String cluster = JsonUtil.GSON.toJson( config );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @QueryParam( "config" ) String config )
    {

        SqoopConfig sqoopConfig = JsonUtil.fromJson( config, SqoopConfig.class );

        UUID uuid = sqoopManager.installCluster( sqoopConfig );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }
    

    @DELETE
    @Path( "clusters/{clusterName}/nodes/{hostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyNode( @PathParam( "clusterName" ) String clusterName,
                                 @PathParam( "hostname" ) String hostname )
    {
        UUID uuid = sqoopManager.destroyNode( clusterName, hostname );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "importData" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response importData( @QueryParam( "dataSourceType" ) String dataSourceType,
                                @QueryParam( "importAllTables" ) String importAllTables,
                                @QueryParam( "datasourceDatabase" ) String datasourceDatabase,
                                @QueryParam( "datasourceTableName" ) String datasourceTableName,
                                @QueryParam( "datasourceColumnFamily" ) String datasourceColumnFamily )
    {
        ImportSetting settings = new ImportSetting();

        DataSourceType type = DataSourceType.valueOf( dataSourceType );
        settings.setType( type );

        if ( !StringUtils.isEmpty( importAllTables ) )
        {
            settings.addParameter( ImportParameter.IMPORT_ALL_TABLES, importAllTables );
        }

        if ( !StringUtils.isEmpty( datasourceDatabase ) )
        {
            settings.addParameter( ImportParameter.DATASOURCE_DATABASE, datasourceDatabase );
        }

        if ( !StringUtils.isEmpty( datasourceTableName ) )
        {
            settings.addParameter( ImportParameter.DATASOURCE_TABLE_NAME, datasourceTableName );
        }

        if ( !StringUtils.isEmpty( datasourceColumnFamily ) )
        {
            settings.addParameter( ImportParameter.DATASOURCE_COLUMN_FAMILY, datasourceColumnFamily );
        }

        UUID uuid = sqoopManager.importData( settings );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @GET
    @Path( "exportData" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response exportData( @QueryParam( "hdfsPath" ) String hdfsPath )
    {
        ExportSetting setting = new ExportSetting();
        setting.setHdfsPath( hdfsPath );

        UUID uuid = sqoopManager.exportData( setting );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}

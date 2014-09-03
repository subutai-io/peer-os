package org.safehaus.subutai.sqoop.services;


import com.google.common.base.Strings;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.api.sqoop.DataSourceType;
import org.safehaus.subutai.api.sqoop.Sqoop;
import org.safehaus.subutai.api.sqoop.setting.ExportSetting;
import org.safehaus.subutai.api.sqoop.setting.ImportParameter;
import org.safehaus.subutai.api.sqoop.setting.ImportSetting;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Sqoop sqoopManager;

    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setSqoopManager( Sqoop sqoopManager )
    {
        this.sqoopManager = sqoopManager;
    }


    @GET
    @Path("getClusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getClusters()
    {

        List<Config> configs = sqoopManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( Config config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }

        return JsonUtil.GSON.toJson( clusterNames );
    }


    @GET
    @Path("getCluster")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCluster(
        @QueryParam("clusterName") String clusterName
    )
    {
        Config config = sqoopManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    @GET
    @Path("installCluster")
    @Produces({ MediaType.APPLICATION_JSON })
    public String installCluster(
        @QueryParam("clusterName") String clusterName,
        @QueryParam("nodes") String nodes
    )
    {

        Config config = new Config();
        config.setClusterName( clusterName );

        if ( nodes != null )
        {
            String[] arr = nodes.split( "[,;]" );
            for ( String s : arr )
            {
                Agent a = agentManager.getAgentByHostname( s );
                if ( a != null )
                {
                    config.getNodes().add( a );
                }
            }
        }

        UUID uuid = sqoopManager.installCluster( config );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path("addNode")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNode(
        @QueryParam("clusterName") String clusterName,
        @QueryParam("hostname") String hostname
    )
    {
        UUID uuid = sqoopManager.addNode( clusterName, hostname );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path("destroyNode")
    @Produces({ MediaType.APPLICATION_JSON })
    public String destroyNode(
        @QueryParam("clusterName") String clusterName,
        @QueryParam("hostname") String hostname
    )
    {
        UUID uuid = sqoopManager.destroyNode( clusterName, hostname );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path("importData")
    @Produces({ MediaType.APPLICATION_JSON })
    public String importData(
        @QueryParam("dataSourceType") String dataSourceType,
        @QueryParam("importAllTables") String importAllTables,
        @QueryParam("datasourceDatabase") String datasourceDatabase,
        @QueryParam("datasourceTableName") String datasourceTableName,
        @QueryParam("datasourceColumnFamily") String datasourceColumnFamily

    )
    {
        ImportSetting settings = new ImportSetting();

        DataSourceType type = DataSourceType.valueOf( dataSourceType );
        settings.setType( type );

        if ( !Strings.isNullOrEmpty( importAllTables ) )
        {
            settings.addParameter( ImportParameter.IMPORT_ALL_TABLES, importAllTables );
        }

        if ( !Strings.isNullOrEmpty( datasourceDatabase ) )
        {
            settings.addParameter( ImportParameter.DATASOURCE_DATABASE, datasourceDatabase );
        }

        if ( !Strings.isNullOrEmpty( datasourceTableName ) )
        {
            settings.addParameter( ImportParameter.DATASOURCE_TABLE_NAME, datasourceTableName );
        }

        if ( !Strings.isNullOrEmpty( datasourceColumnFamily ) )
        {
            settings.addParameter( ImportParameter.DATASOURCE_COLUMN_FAMILY, datasourceColumnFamily );
        }

        UUID uuid = sqoopManager.importData( settings );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path("exportData")
    @Produces({ MediaType.APPLICATION_JSON })
    public String exportData(
        @QueryParam("hdfsPath") String hdfsPath
    )
    {
        ExportSetting setting = new ExportSetting();
        setting.setHdfsPath( hdfsPath );

        UUID uuid = sqoopManager.exportData( setting );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }
}

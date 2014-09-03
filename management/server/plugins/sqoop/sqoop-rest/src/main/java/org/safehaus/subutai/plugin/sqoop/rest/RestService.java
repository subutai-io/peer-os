package org.safehaus.subutai.plugin.sqoop.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.sqoop.api.DataSourceType;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.setting.ExportSetting;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportParameter;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;

public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Sqoop sqoopManager;

    private AgentManager agentManager;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setSqoopManager(Sqoop sqoopManager) {
        this.sqoopManager = sqoopManager;
    }

    @GET
    @Path("clusters")
    @Produces({MediaType.APPLICATION_JSON})
    public String getClusters() {

        List<SqoopConfig> configs = sqoopManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for(SqoopConfig config : configs) {
            clusterNames.add(config.getClusterName());
        }

        return JsonUtil.GSON.toJson(clusterNames);
    }

    @GET
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getCluster(
            @PathParam("clusterName") String clusterName
    ) {
        SqoopConfig config = sqoopManager.getCluster(clusterName);

        return JsonUtil.GSON.toJson(config);
    }

    @POST
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String installCluster(
            @PathParam("clusterName") String clusterName,
            @QueryParam("nodes") String nodes
    ) {

        SqoopConfig config = new SqoopConfig();
        config.setClusterName(clusterName);

        if(nodes != null) {
            String[] arr = nodes.split("[,;]");
            for(String s : arr) {
                Agent a = agentManager.getAgentByHostname(s);
                if(a != null) config.getNodes().add(a);
            }
        }

        UUID uuid = sqoopManager.installCluster(config);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @POST
    @Path("clusters/{clusterName}/nodes/{hostname}")
    @Produces({MediaType.APPLICATION_JSON})
    @Deprecated()
    public String addNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
    ) {
        return JsonUtil.toJson(OPERATION_ID, null);
    }

    @DELETE
    @Path("clusters/{clusterName}/nodes/{hostname}")
    @Produces({MediaType.APPLICATION_JSON})
    public String destroyNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
    ) {
        UUID uuid = sqoopManager.destroyNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @POST
    @Path("importData")
    @Produces({MediaType.APPLICATION_JSON})
    public String importData(
            @QueryParam("dataSourceType") String dataSourceType,
            @QueryParam("importAllTables") String importAllTables,
            @QueryParam("datasourceDatabase") String datasourceDatabase,
            @QueryParam("datasourceTableName") String datasourceTableName,
            @QueryParam("datasourceColumnFamily") String datasourceColumnFamily
    ) {
        ImportSetting settings = new ImportSetting();

        DataSourceType type = DataSourceType.valueOf(dataSourceType);
        settings.setType(type);

        if(!StringUtils.isEmpty(importAllTables))
            settings.addParameter(ImportParameter.IMPORT_ALL_TABLES, importAllTables);

        if(!StringUtils.isEmpty(datasourceDatabase))
            settings.addParameter(ImportParameter.DATASOURCE_DATABASE, datasourceDatabase);

        if(!StringUtils.isEmpty(datasourceTableName))
            settings.addParameter(ImportParameter.DATASOURCE_TABLE_NAME, datasourceTableName);

        if(!StringUtils.isEmpty(datasourceColumnFamily))
            settings.addParameter(ImportParameter.DATASOURCE_COLUMN_FAMILY, datasourceColumnFamily);

        UUID uuid = sqoopManager.importData(settings);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("exportData")
    @Produces({MediaType.APPLICATION_JSON})
    public String exportData(
            @QueryParam("hdfsPath") String hdfsPath
    ) {
        ExportSetting setting = new ExportSetting();
        setting.setHdfsPath(hdfsPath);

        UUID uuid = sqoopManager.exportData(setting);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }
}

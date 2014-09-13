/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.api;


import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;

import java.util.List;
import java.util.UUID;


/**
 * @author dilshat
 */
public interface HBase extends ApiBase<HBaseClusterConfig> {

    public UUID installCluster(HBaseClusterConfig config);

    public List<HBaseClusterConfig> getClusters();

    UUID startCluster(String clusterName);

    //    UUID checkNode(HBaseType type, String clusterName, String lxcHostname);

    //    UUID startNodes(String clusterName);

    //    UUID stopNodes(String clusterName);

    UUID stopCluster(String clusterName);

    UUID checkCluster(String clusterName);

    //    List<HadoopClusterConfig> getHadoopClusters();

    //    HadoopClusterConfig getHadoopCluster( String clusterName );

    public ClusterSetupStrategy getClusterSetupStrategy(Environment environment, HBaseClusterConfig config,
                                                        ProductOperation po);

    public EnvironmentBuildTask getDefaultEnvironmentBlueprint(HBaseClusterConfig config);

    UUID checkNode(String clustername, String lxchostname);

    UUID destroyNode(String clustername, String lxchostname, String nodetype);

    UUID addNode(String clustername, String lxchostname, String nodetype);

    UUID destroyCluster(String clusterName);
}

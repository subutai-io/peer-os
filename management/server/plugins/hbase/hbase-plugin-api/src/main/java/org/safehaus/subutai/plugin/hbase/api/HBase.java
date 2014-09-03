/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


/**
 * @author dilshat
 */
public interface HBase extends ApiBase<HBaseConfig> {

    public UUID installCluster( HBaseConfig config );

    UUID startCluster( String clusterName );

    UUID stopCluster( String clusterName );

    //    UUID checkNode(HBaseType type, String clusterName, String lxcHostname);

    //    UUID startNodes(String clusterName);

    //    UUID stopNodes(String clusterName);

    UUID checkCluster( String clusterName );

    public List<HBaseConfig> getClusters();

    List<HadoopClusterConfig> getHadoopClusters();

    HadoopClusterConfig getHadoopCluster( String clusterName );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, HBaseConfig config,
                                                         ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( HBaseConfig config );
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.hbase;


import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.List;
import java.util.UUID;


/**
 * @author dilshat
 */
public interface HBase extends ApiBase<HBaseConfig>
{

    public UUID installCluster( HBaseConfig config );

    UUID startCluster( String clusterName );

    UUID stopCluster( String clusterName );

    //    UUID checkNode(HBaseType type, String clusterName, String lxcHostname);

    //    UUID startNodes(String clusterName);

    //    UUID stopNodes(String clusterName);

    UUID checkCluster( String clusterName );

    public List<HBaseConfig> getClusters();

    List<Config> getHadoopClusters();

    Config getHadoopCluster( String clusterName );
}

package org.safehaus.subutai.plugin.hbase.api.Cluster;


import java.util.Set;


/**
 * Created by bahadyr on 11/14/14.
 */
public class HBaseCluster implements Cluster
{

    Set<ClusterNode> clusterNodes;


    @Override
    public void addNode( final ClusterNode clusterNode )
    {
        //TODO:
    }


    @Override
    public void removeNode( final ClusterNode clusterNode )
    {
        //TODO: remove node from

    }


    @Override
    public void configureNode( final NodeConfiguration nodeConfiguration )
    {

    }
}

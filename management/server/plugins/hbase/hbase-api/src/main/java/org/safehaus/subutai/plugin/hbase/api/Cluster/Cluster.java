package org.safehaus.subutai.plugin.hbase.api.Cluster;


/**
 * Created by bahadyr on 11/14/14.
 */
public interface Cluster
{

    public void addNode(ClusterNode clusterNode);

    public void removeNode(ClusterNode clusterNode);

    public void configureNode(NodeConfiguration nodeConfiguration);


}

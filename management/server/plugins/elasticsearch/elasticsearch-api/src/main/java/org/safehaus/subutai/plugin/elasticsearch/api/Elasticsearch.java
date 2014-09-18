package org.safehaus.subutai.plugin.elasticsearch.api;


import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;

import java.util.UUID;


public interface Elasticsearch extends ApiBase<ElasticsearchClusterConfiguration > {

    public UUID startAllNodes(String clusterName);

    public UUID checkAllNodes(String clusterName);

    public UUID stopAllNodes(String clusterName);

    public UUID addNode(String clusterName, String lxcHostname);

    public UUID checkNode(String clusterName, String lxcHostname);

    public UUID startNode(String clusterName, String lxcHostname);

    public UUID stopNode(String clusterName, String lxcHostname);

    public UUID destroyNode(String clusterName, String lxcHostname);

    ClusterSetupStrategy getClusterSetupStrategy( Environment environment, ElasticsearchClusterConfiguration elasticsearchClusterConfiguration, ProductOperation po );
}

package org.safehaus.subutai.plugin.flume.api;

import java.util.UUID;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

public interface Flume extends ApiBase<FlumeConfig> {

    public UUID installCluster(FlumeConfig config, HadoopClusterConfig hadoopConfig);

    public UUID startNode(String clusterName, String lxcHostname);

    public UUID stopNode(String clusterName, String lxcHostname);

    public UUID checkNode(String clusterName, String lxcHostname);

    public UUID addNode(String clusterName);

    public UUID addNode(String clusterName, String lxcHostname);

    public UUID destroyNode(String clusterName, String lxcHostname);

    public ClusterSetupStrategy getClusterSetupStrategy(Environment env,
            FlumeConfig config, ProductOperation po);

}

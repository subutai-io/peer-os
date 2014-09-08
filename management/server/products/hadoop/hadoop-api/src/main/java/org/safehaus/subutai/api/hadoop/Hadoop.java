package org.safehaus.subutai.api.hadoop;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;

/**
 * Created by daralbaev on 02.04.14.
 */
public interface Hadoop extends ApiBase<HadoopClusterConfig> {

	public UUID startNameNode(HadoopClusterConfig hadoopClusterConfig );

	public UUID stopNameNode(HadoopClusterConfig hadoopClusterConfig );

	public UUID restartNameNode(HadoopClusterConfig hadoopClusterConfig );

	public UUID statusNameNode(HadoopClusterConfig hadoopClusterConfig );

	public UUID statusSecondaryNameNode(HadoopClusterConfig hadoopClusterConfig );

	public UUID statusDataNode(Agent agent);

	public UUID startJobTracker(HadoopClusterConfig hadoopClusterConfig );

	public UUID stopJobTracker(HadoopClusterConfig hadoopClusterConfig );

	public UUID restartJobTracker(HadoopClusterConfig hadoopClusterConfig );

	public UUID statusJobTracker(HadoopClusterConfig hadoopClusterConfig );

	public UUID statusTaskTracker(Agent agent);

	public UUID addNode(String clusterName);

	public UUID blockDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent);

	public UUID blockTaskTracker(HadoopClusterConfig hadoopClusterConfig, Agent agent);

	public UUID unblockDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent);

	public UUID unblockTaskTracker(HadoopClusterConfig hadoopClusterConfig, Agent agent);

}

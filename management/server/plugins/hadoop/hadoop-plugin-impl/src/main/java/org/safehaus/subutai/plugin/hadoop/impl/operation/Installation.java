package org.safehaus.subutai.plugin.hadoop.impl.operation;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopDbSetupStrategy;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import java.util.UUID;

public class Installation {
	private HadoopImpl parent;
	private HadoopClusterConfig hadoopClusterConfig;

	public Installation(HadoopImpl parent, HadoopClusterConfig hadoopClusterConfig) {
		this.parent = parent;
		this.hadoopClusterConfig = hadoopClusterConfig;
	}

	public UUID execute() {
		final ProductOperation po = HadoopImpl.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY, "Installation of Hadoop");

		HadoopImpl.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				HadoopDbSetupStrategy strategy = new HadoopDbSetupStrategy(po, parent, hadoopClusterConfig);
				try {
					strategy.setup();
				} catch (ClusterSetupException e) {
					po.addLogFailed(e.getMessage());
				}
			}
		});

		return po.getId();
	}
}

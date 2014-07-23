package org.safehaus.subutai.hadoop.impl.operation;

import org.safehaus.subutai.hadoop.api.Config;
import org.safehaus.subutai.hadoop.impl.HadoopDbSetupStrategy;
import org.safehaus.subutai.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;

import java.util.UUID;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Installation {
	private HadoopImpl parent;
	private Config config;

	public Installation(HadoopImpl parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public UUID execute() {
		final ProductOperation po = parent.getTracker().createProductOperation(Config.PRODUCT_KEY, "Installation of Hadoop");

		parent.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				HadoopDbSetupStrategy strategy = new HadoopDbSetupStrategy(po, parent, HadoopImpl.getContainerManager(), config);
			}
		});

		return po.getId();
	}
}

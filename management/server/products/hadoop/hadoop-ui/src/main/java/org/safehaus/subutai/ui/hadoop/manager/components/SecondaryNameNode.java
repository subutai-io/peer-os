package org.safehaus.subutai.ui.hadoop.manager.components;

import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class SecondaryNameNode extends ClusterNode {

	public SecondaryNameNode(Config cluster) {
		super(cluster);
		setHostname(cluster.getSecondaryNameNode().getHostname());

		restartButton.setVisible(false);
		startButton.setEnabled(false);
		stopButton.setEnabled(false);

		getStatus(null);
	}


	@Override
	protected void getStatus(UUID trackID) {
		setLoading(true);

		HadoopUI.getExecutor().execute(new CheckTask(cluster, new CompleteEvent() {

			public void onComplete(NodeState state) {
				synchronized (progressButton) {
					boolean isRunning = false;
					if (state == NodeState.RUNNING) {
						isRunning = true;
					} else if (state == NodeState.STOPPED) {
						isRunning = false;
					}

					setLoading(false);
					startButton.setVisible(isRunning);
					stopButton.setVisible(!isRunning);
				}
			}
		}, trackID, cluster.getSecondaryNameNode()));
	}

	@Override
	protected void setLoading(boolean isLoading) {
		startButton.setVisible(!isLoading);
		stopButton.setVisible(!isLoading);
		progressButton.setVisible(isLoading);
	}
}

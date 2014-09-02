package org.safehaus.subutai.ui.hadoop.manager.components;

import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class SlaveNode extends ClusterNode {

	private Agent agent;
	private boolean isDataNode;

	public SlaveNode(Config cluster, Agent agent, boolean isDataNode) {
		super(cluster);
		this.agent = agent;
		this.isDataNode = isDataNode;

		setHostname(agent.getHostname());

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
		}, trackID, agent, isDataNode));
	}

	@Override
	protected void setLoading(boolean isLoading) {
		startButton.setVisible(!isLoading);
		stopButton.setVisible(!isLoading);
		progressButton.setVisible(isLoading);
	}

	public Agent getAgent() {
		return agent;
	}
}

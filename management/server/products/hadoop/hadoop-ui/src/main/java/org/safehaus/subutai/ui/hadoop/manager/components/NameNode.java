package org.safehaus.subutai.ui.hadoop.manager.components;

import com.vaadin.event.MouseEvents;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class NameNode extends ClusterNode {

	public NameNode(final HadoopClusterConfig cluster) {
		super(cluster);
		setHostname(cluster.getNameNode().getHostname());

		startButton.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent clickEvent) {
				setLoading(true);
				getStatus(HadoopUI.getHadoopManager().startNameNode(cluster));
			}
		});

		stopButton.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent clickEvent) {
				setLoading(true);
				getStatus(HadoopUI.getHadoopManager().stopNameNode(cluster));
			}
		});

		restartButton.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent clickEvent) {
				setLoading(true);
				getStatus(HadoopUI.getHadoopManager().restartNameNode(cluster));
			}
		});

		getStatus(null);
	}

	@Override
	protected void getStatus(UUID trackID) {
		setLoading(true);
		for (ClusterNode slaveNode : slaveNodes) {
			slaveNode.setLoading(true);
		}

		HadoopUI.getExecutor().execute(new CheckTask(cluster, new CompleteEvent() {

			public void onComplete(NodeState state) {
				synchronized (progressButton) {
					boolean isRunning = false;
					if (state == NodeState.RUNNING) {
						isRunning = true;
					} else if (state == NodeState.STOPPED) {
						isRunning = false;
					}

					startButton.setEnabled(!isRunning);
					restartButton.setEnabled(isRunning);
					stopButton.setEnabled(isRunning);

					for (ClusterNode slaveNode : slaveNodes) {
						slaveNode.getStatus(null);
					}

					setLoading(false);
				}
			}
		}, trackID, cluster.getNameNode()));

	}

	@Override
	protected void setLoading(boolean isLoading) {
		startButton.setVisible(!isLoading);
		stopButton.setVisible(!isLoading);
		restartButton.setVisible(!isLoading);
		progressButton.setVisible(isLoading);
	}
}

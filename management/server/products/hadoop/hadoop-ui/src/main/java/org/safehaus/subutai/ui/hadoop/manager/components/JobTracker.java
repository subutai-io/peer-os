package org.safehaus.subutai.ui.hadoop.manager.components;

import com.vaadin.event.MouseEvents;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class JobTracker extends ClusterNode {

	public JobTracker(final Config cluster) {
		super(cluster);
		setHostname(cluster.getJobTracker().getHostname());

		startButton.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent clickEvent) {
				setLoading(true);
				getStatus(HadoopUI.getHadoopManager().startJobTracker(cluster));
			}
		});

		stopButton.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent clickEvent) {
				setLoading(true);
				getStatus(HadoopUI.getHadoopManager().stopJobTracker(cluster));
			}
		});

		restartButton.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent clickEvent) {
				setLoading(true);
				getStatus(HadoopUI.getHadoopManager().restartJobTracker(cluster));
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
		}, trackID, cluster.getJobTracker()));
	}

	@Override
	protected void setLoading(boolean isLoading) {
		startButton.setVisible(!isLoading);
		stopButton.setVisible(!isLoading);
		restartButton.setVisible(!isLoading);
		progressButton.setVisible(isLoading);
	}
}

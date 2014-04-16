package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class SecondaryNameNode extends ClusterNode {

    public SecondaryNameNode(Config cluster) {
        super(cluster);

        restartButton.setVisible(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
    }


    @Override
    protected void getStatus(UUID trackID) {
        setLoading(true);

        HadoopUI.getExecutor().execute(new CheckTask(cluster, new CompleteEvent() {

            public void onComplete(NodeState state) {
                synchronized (progressIcon) {
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
        progressIcon.setVisible(isLoading);
    }
}

package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.ui.Button;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class JobTracker extends ClusterNode {

    public JobTracker(final Config cluster) {
        super(cluster);

        startButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setLoading(true);
                getStatus(HadoopUI.getHadoopManager().startJobTracker(cluster));
            }
        });

        stopButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setLoading(true);
                getStatus(HadoopUI.getHadoopManager().stopJobTracker(cluster));
            }
        });

        restartButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setLoading(true);
                getStatus(HadoopUI.getHadoopManager().restartJobTracker(cluster));
            }
        });
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

                    startButton.setEnabled(!isRunning);
                    restartButton.setEnabled(isRunning);
                    stopButton.setEnabled(isRunning);

                    setLoading(false);
                }
            }
        }, trackID));
    }

    @Override
    protected void setLoading(boolean isLoading) {
        startButton.setVisible(!isLoading);
        stopButton.setVisible(!isLoading);
        restartButton.setVisible(!isLoading);
        progressIcon.setVisible(isLoading);
    }
}

package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components.namenode;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;
import org.safehaus.kiskis.mgmt.ui.hadoop.manager.components.ClusterNode;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class NameNode extends ClusterNode {
    private Embedded progressIcon;
    private Config cluster;
    private Button startButton, stopButton, restartButton;

    public NameNode(Config cluster) {
        this.cluster = cluster;

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressIcon());
        addComponent(getStartButton());
        addComponent(getStopButton());
        addComponent(getRestartButton());

        getStatus(null);
    }

    private Embedded getProgressIcon() {
        progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressIcon.setVisible(false);

        return progressIcon;
    }

    private Button getStartButton() {
        startButton = new Button();
        startButton.setIcon(new ThemeResource("icons/buttons/start.png"));
        startButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setLoading(true);
                getStatus(HadoopUI.getHadoopManager().startNameNode(cluster));
            }
        });


        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button();
        stopButton.setIcon(new ThemeResource("icons/buttons/stop.png"));
        stopButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setLoading(true);
                getStatus(HadoopUI.getHadoopManager().stopNameNode(cluster));
            }
        });

        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button();
        restartButton.setIcon(new ThemeResource("icons/buttons/restart.png"));
        restartButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setLoading(true);
                getStatus(HadoopUI.getHadoopManager().restartNameNode(cluster));
            }
        });

        return restartButton;
    }

    @Override
    public void getStatus(UUID trackID) {
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

    private void setLoading(boolean isLoading) {
        startButton.setVisible(!isLoading);
        stopButton.setVisible(!isLoading);
        restartButton.setVisible(!isLoading);
        progressIcon.setVisible(isLoading);
    }
}

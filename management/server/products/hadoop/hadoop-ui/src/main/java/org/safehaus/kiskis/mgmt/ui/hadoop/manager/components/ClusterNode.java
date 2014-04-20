package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class ClusterNode extends HorizontalLayout {

    protected Button progressButton;
    protected Config cluster;
    protected Button startButton, stopButton, restartButton;
    protected List<ClusterNode> slaveNodes;

    public ClusterNode(Config cluster) {
        this.cluster = cluster;
        slaveNodes = new ArrayList<ClusterNode>();

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressButton());
        setComponentAlignment(startButton, Alignment.MIDDLE_CENTER);
        addComponent(getStartButton());
        addComponent(getStopButton());
        addComponent(getRestartButton());
    }

    private Button getProgressButton() {
        progressButton = new Button();
        progressButton.setIcon(new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressButton.setVisible(false);

        return progressButton;
    }

    private Button getStartButton() {
        startButton = new Button();
        startButton.setIcon(new ThemeResource("icons/buttons/start.png"));

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button();
        startButton.setIcon(new ThemeResource("icons/buttons/stop.png"));

        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button();
        startButton.setIcon(new ThemeResource("icons/buttons/restart.png"));

        return restartButton;
    }

    public void addSlaveNode(ClusterNode slaveNode) {
        slaveNodes.add(slaveNode);
    }

    protected void getStatus(UUID trackID) {
    }

    protected void setLoading(boolean isLoading) {

    }
}

package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class ClusterNode extends HorizontalLayout {

    protected Config cluster;
    protected Embedded progressButton, startButton, stopButton, restartButton;
    protected List<ClusterNode> slaveNodes;

    public ClusterNode(Config cluster) {
        this.cluster = cluster;
        slaveNodes = new ArrayList<ClusterNode>();

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressButton());
        addComponent(getStartButton());
        addComponent(getStopButton());
        addComponent(getRestartButton());
    }

    private Embedded getProgressButton() {
        progressButton = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressButton.setWidth(24, UNITS_PIXELS);
        progressButton.setHeight(24, UNITS_PIXELS);
        progressButton.setVisible(false);

        return progressButton;
    }

    private Embedded getStartButton() {
        startButton = new Embedded("", new ThemeResource("icons/buttons/start.png"));
        startButton.setWidth(24, UNITS_PIXELS);
        startButton.setHeight(24, UNITS_PIXELS);

        return startButton;
    }

    private Embedded getStopButton() {
        stopButton = new Embedded("", new ThemeResource("icons/buttons/stop.png"));
        stopButton.setWidth(24, UNITS_PIXELS);
        stopButton.setHeight(24, UNITS_PIXELS);

        return stopButton;
    }

    private Embedded getRestartButton() {
        restartButton = new Embedded("", new ThemeResource("icons/buttons/stop.png"));
        restartButton.setWidth(24, UNITS_PIXELS);
        restartButton.setHeight(24, UNITS_PIXELS);

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

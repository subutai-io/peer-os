package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class ClusterNode extends HorizontalLayout {

    public static final int ICON_SIZE = 18;

    protected Config cluster;
    protected Embedded progressButton, startButton, stopButton, restartButton;
    protected List<ClusterNode> slaveNodes;
    protected Label hostname;

    public ClusterNode(Config cluster) {
        this.cluster = cluster;
        slaveNodes = new ArrayList<ClusterNode>();

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressButton());
        setComponentAlignment(progressButton, Alignment.TOP_CENTER);
        addComponent(getStartButton());
        setComponentAlignment(startButton, Alignment.TOP_CENTER);
        addComponent(getStopButton());
        setComponentAlignment(stopButton, Alignment.TOP_CENTER);
        addComponent(getRestartButton());
        setComponentAlignment(restartButton, Alignment.TOP_CENTER);
        addComponent(getHostnameLabel());
        setComponentAlignment(hostname, Alignment.TOP_CENTER);
    }

    private Label getHostnameLabel() {
        hostname = new Label("");
        return hostname;
    }

    public void setHostname(String value) {
        hostname.setValue(value);
    }

    private Embedded getProgressButton() {
        progressButton = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressButton.setWidth(ICON_SIZE + 2, UNITS_PIXELS);
        progressButton.setHeight(ICON_SIZE + 2, UNITS_PIXELS);
        progressButton.setVisible(false);

        return progressButton;
    }

    private Embedded getStartButton() {
        startButton = new Embedded("", new ThemeResource("icons/buttons/start.png"));
        startButton.setDescription("Start");
        startButton.setWidth(ICON_SIZE, UNITS_PIXELS);
        startButton.setHeight(ICON_SIZE, UNITS_PIXELS);

        return startButton;
    }

    private Embedded getStopButton() {
        stopButton = new Embedded("", new ThemeResource("icons/buttons/stop.png"));
        stopButton.setDescription("Stop");
        stopButton.setWidth(ICON_SIZE, UNITS_PIXELS);
        stopButton.setHeight(ICON_SIZE, UNITS_PIXELS);

        return stopButton;
    }

    private Embedded getRestartButton() {
        restartButton = new Embedded("", new ThemeResource("icons/buttons/restart.png"));
        restartButton.setDescription("Restart");
        restartButton.setWidth(ICON_SIZE, UNITS_PIXELS);
        restartButton.setHeight(ICON_SIZE, UNITS_PIXELS);

        return restartButton;
    }

    public void addSlaveNode(ClusterNode slaveNode) {
        slaveNodes.add(slaveNode);
    }

    protected void getStatus(UUID trackID) {
    }

    protected void setLoading(boolean isLoading) {

    }

    public Config getCluster() {
        return cluster;
    }
}

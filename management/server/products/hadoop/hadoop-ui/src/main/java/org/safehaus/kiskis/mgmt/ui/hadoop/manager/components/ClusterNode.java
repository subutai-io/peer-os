package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.Sizeable;
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

    protected Label progressIcon;
    protected Config cluster;
    protected Embedded startButton, stopButton, restartButton;
    protected List<ClusterNode> slaveNodes;

    public ClusterNode(Config cluster) {
        this.cluster = cluster;
        slaveNodes = new ArrayList<ClusterNode>();

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressIcon());
        setComponentAlignment(startButton, Alignment.MIDDLE_CENTER);
        addComponent(getStartButton());
        addComponent(getStopButton());
        addComponent(getRestartButton());
    }

    private Label getProgressIcon() {
        progressIcon = new Label();
        progressIcon.setIcon(new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressIcon.setContentMode(Label.CONTENT_XHTML);
        progressIcon.setHeight(24, Sizeable.UNITS_PIXELS);
        progressIcon.setWidth(24, Sizeable.UNITS_PIXELS);
        progressIcon.setVisible(false);

        return progressIcon;
    }

    private Embedded getStartButton() {
        startButton = new Embedded("", new ThemeResource("icons/buttons/start.png"));
        startButton.setWidth(24, Sizeable.UNITS_PIXELS);
        startButton.setHeight(24, Sizeable.UNITS_PIXELS);

        return startButton;
    }

    private Embedded getStopButton() {
        stopButton = new Embedded("", new ThemeResource("icons/buttons/stop.png"));
        stopButton.setWidth(24, Sizeable.UNITS_PIXELS);
        stopButton.setHeight(24, Sizeable.UNITS_PIXELS);

        return stopButton;
    }

    private Embedded getRestartButton() {
        restartButton = new Embedded("", new ThemeResource("icons/buttons/restart.png"));
        restartButton.setWidth(24, Sizeable.UNITS_PIXELS);
        restartButton.setHeight(24, Sizeable.UNITS_PIXELS);

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

package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
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
    protected Button startButton, stopButton, restartButton;
    protected List<SlaveNode> slaveNodes;

    public ClusterNode(Config cluster) {
        this.cluster = cluster;
        slaveNodes = new ArrayList<SlaveNode>();

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressIcon());
        addComponent(getStartButton());
        addComponent(getStopButton());
        addComponent(getRestartButton());
    }

    private Label getProgressIcon() {
        progressIcon = new Label();
        progressIcon.setIcon(new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressIcon.setContentMode(Label.CONTENT_XHTML);
        progressIcon.setHeight(11, Sizeable.UNITS_PIXELS);
        progressIcon.setWidth(50, Sizeable.UNITS_PIXELS);
        progressIcon.setVisible(false);

        return progressIcon;
    }

    private Button getStartButton() {
        startButton = new Button();
        startButton.setIcon(new ThemeResource("icons/buttons/start.png"));

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button();
        stopButton.setIcon(new ThemeResource("icons/buttons/stop.png"));

        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button();
        restartButton.setIcon(new ThemeResource("icons/buttons/restart.png"));

        return restartButton;
    }

    public void addSlaveNode(SlaveNode slaveNode) {
        slaveNodes.add(slaveNode);
    }

    protected void getStatus(UUID trackID) {
    }

    protected void setLoading(boolean isLoading) {

    }
}

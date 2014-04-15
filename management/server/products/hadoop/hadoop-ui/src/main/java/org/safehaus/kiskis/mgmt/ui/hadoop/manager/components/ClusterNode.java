package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class ClusterNode extends HorizontalLayout {

    protected Embedded progressIcon;
    protected Config cluster;
    protected Button startButton, stopButton, restartButton;

    public ClusterNode(Config cluster) {
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

    protected void getStatus(UUID trackID) {

    }

    protected void setLoading(boolean isLoading) {

    }
}

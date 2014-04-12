package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;

/**
 * Created by daralbaev on 12.04.14.
 */
public class SecondaryNameNode extends HorizontalLayout implements ClusterNode {
    private Embedded progressIcon;
    private Config cluster;
    private Button startButton, stopButton;

    public SecondaryNameNode(Config cluster) {
        this.cluster = cluster;

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressIcon());
        addComponent(getStartButton());
        addComponent(getStopButton());
    }

    private Embedded getProgressIcon() {
        progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressIcon.setVisible(false);

        return progressIcon;
    }

    private Button getStartButton() {
        startButton = new Button();
        startButton.setIcon(new ThemeResource("icons/buttons/start.png"));
        startButton.setEnabled(false);

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button();
        stopButton.setIcon(new ThemeResource("icons/buttons/stop.png"));
        stopButton.setEnabled(false);

        return stopButton;
    }

    @Override
    public void getStatus() {

    }
}

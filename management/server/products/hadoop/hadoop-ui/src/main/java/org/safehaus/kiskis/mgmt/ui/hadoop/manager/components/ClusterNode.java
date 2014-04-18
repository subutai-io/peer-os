package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.event.MouseEvents;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
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
    protected Button startButton, stopButton, restartButton;
    protected Embedded image;
    protected List<ClusterNode> slaveNodes;

    public ClusterNode(Config cluster) {
        this.cluster = cluster;
        slaveNodes = new ArrayList<ClusterNode>();

        setMargin(true);
        setSpacing(true);

        addComponent(getProgressIcon());
        addComponent(getStartButton());
        addComponent(getStopButton());
        addComponent(getRestartButton());
        addComponent(getImage());
    }

    private Embedded getImage() {
        image = new Embedded("", new ThemeResource("icons/buttons/start.png"));
        image.setWidth(16, Sizeable.UNITS_PIXELS);
        image.setHeight(16, Sizeable.UNITS_PIXELS);
        image.addListener(new MouseEvents.ClickListener() {

            @Override
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                setLoading(true);
            }
        });

        return image;
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
        startButton.setWidth(50, Sizeable.UNITS_PIXELS);
        startButton.setHeight(11, Sizeable.UNITS_PIXELS);

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button();
        stopButton.setIcon(new ThemeResource("icons/buttons/stop.png"));
        stopButton.setWidth(50, Sizeable.UNITS_PIXELS);
        stopButton.setHeight(11, Sizeable.UNITS_PIXELS);

        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button();
        restartButton.setIcon(new ThemeResource("icons/buttons/restart.png"));
        restartButton.setWidth(50, Sizeable.UNITS_PIXELS);
        restartButton.setHeight(11, Sizeable.UNITS_PIXELS);

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

package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;

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

//        getStatus();
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
                if (HadoopUI.getHadoopManager().startNameNode(cluster)) {
                    getStatus();
                }
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
                if (HadoopUI.getHadoopManager().stopNameNode(cluster)) {
                    getStatus();
                }
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
                if (HadoopUI.getHadoopManager().restartNameNode(cluster)) {
                    getStatus();
                }
            }
        });

        return restartButton;
    }

    @Override
    public void getStatus() {
        setLoading(true);

        boolean isRunning = HadoopUI.getHadoopManager().statusNameNode(cluster);
        startButton.setEnabled(isRunning);
        restartButton.setEnabled(!isRunning);
        stopButton.setEnabled(isRunning);

        setLoading(false);
    }

    private void setLoading(boolean isLoading) {
        startButton.setVisible(!isLoading);
        stopButton.setVisible(!isLoading);
        restartButton.setVisible(!isLoading);
        progressIcon.setVisible(isLoading);
    }
}

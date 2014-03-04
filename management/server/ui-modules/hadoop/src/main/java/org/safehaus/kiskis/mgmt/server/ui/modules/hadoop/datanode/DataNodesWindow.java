package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.datanode;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import static org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus.SUCCESS;

public final class DataNodesWindow extends Window {

    private Button startButton, stopButton, restartButton;
    private Label indicator, statusLabel;
    private DataNodesTable dataNodesTable;
    
    private final HadoopClusterInfo cluster;

    public DataNodesWindow(String clusterName) {        
        setModal(true);
        setCaption("Hadoop Data Node Configuration");

        this.cluster = HadoopDAO.getHadoopClusterInfo(clusterName);
        getStatusLabel();
        getIndicator();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(900, Sizeable.UNITS_PIXELS);
        verticalLayout.setHeight(450, Sizeable.UNITS_PIXELS);
        verticalLayout.setSpacing(true);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        buttonLayout.addComponent(getStartButton());
        buttonLayout.addComponent(getStopButton());
        buttonLayout.addComponent(getRestartButton());
        buttonLayout.addComponent(getStatusLabel());
        buttonLayout.addComponent(getIndicator());

        Panel panel = new Panel();
        panel.setSizeFull();
        panel.addComponent(buttonLayout);
        panel.addComponent(getTable());

        verticalLayout.addComponent(panel);
        setContent(verticalLayout);

        getStatus();
    }
    
    private Button getStartButton() {
        startButton = new Button("Start");
        startButton.setEnabled(false);

        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                disableButtons(0);
                statusLabel.setValue("");
                indicator.setVisible(true);

                HadoopModule.getTaskRunner().executeTask(Tasks.getNameNodeCommand(cluster, "start"), new TaskCallback() {
                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        if (task.isCompleted()) {
                            getStatus();
                        }

                        return null;
                    }
                });
            }
        });

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button("Stop");
        stopButton.setEnabled(false);

        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                disableButtons(0);
                statusLabel.setValue("");
                indicator.setVisible(true);

                HadoopModule.getTaskRunner().executeTask(Tasks.getNameNodeCommand(cluster, "stop"), new TaskCallback() {
                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        if (task.isCompleted()) {
                            getStatus();
                        }

                        return null;
                    }
                });
            }
        });

        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button("Restart");
        restartButton.setEnabled(false);

        restartButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                disableButtons(0);
                statusLabel.setValue("");
                indicator.setVisible(true);

                HadoopModule.getTaskRunner().executeTask(Tasks.getNameNodeCommand(cluster, "restart"), new TaskCallback() {
                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        if (task.isCompleted()) {
                            getStatus();
                        }

                        return null;
                    }
                });
                disableButtons(0);
            }
        });

        return restartButton;
    }

    private void disableButtons(int status) {
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        restartButton.setEnabled(false);

        if (status == 1) {
            stopButton.setEnabled(true);
            restartButton.setEnabled(true);
        }

        if (status == 2) {
            startButton.setEnabled(true);
        }
    }

    private void getStatus() {
        statusLabel.setValue("");
        indicator.setVisible(true);

        HadoopModule.getTaskRunner().executeTask(Tasks.getNameNodeCommand(cluster, "status"), new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == SUCCESS) {
                        String status = parseStatus(stdOut);
                        statusLabel.setValue(status);
                        if (status.trim().equalsIgnoreCase("Running")) {
                            disableButtons(1);
                        } else {
                            disableButtons(2);
                        }
                        indicator.setVisible(false);
                        dataNodesTable.refreshDataSource();
                    }
                }

                return null;
            }
        });
    }

    private Label getStatusLabel() {
        if (statusLabel == null) {
            statusLabel = new Label();
            statusLabel.setValue("");
        }

        return statusLabel;
    }

    private Label getIndicator() {
        if (indicator == null) {
            indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
            indicator.setVisible(false);
        }

        return indicator;
    }

    private DataNodesTable getTable() {
        dataNodesTable = new DataNodesTable(cluster.getClusterName());

        return dataNodesTable;
    }

    private String parseStatus(String response) {
        String[] array = response.split("\\n");

        for (String status : array) {
            if (status.contains("NameNode")) {
                return status.replaceAll("NameNode is ", "");
                        //.replaceAll("\\(SecondaryNameNode is NOT Running\\)", "");
            }
        }

        return "";
    }
}

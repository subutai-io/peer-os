/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author bahadyr
 */
public class Step6 extends FormLayout {

    String dataDirCommand = "sed -i \"s/- \\/var\\/lib\\/cassandra\\/data/- %dataDirg\" /opt/cassandra-2.0.0/conf/cassandra.yaml";
    String commitDirCommand = "sed -i \"s/commitlog_directory: \\/var\\/lib\\/cassandra\\/commitlog/commitlog_directory: %commitDirg\" /opt/cassandra-2.0.0/conf/cassandra.yaml";

    String cacheDirCommand = "sed -i \"s/saved_caches_directory: \\/var\\/lib\\/cassandra\\/saved_caches/saved_caches_directory: %cacheDir/g\" /opt/cassandra-2.0.0/conf/cassandra.yaml";

    public Step6(final CassandraWizard cassandraWizard) {
        setCaption("Change directories");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) Welcome<br>"
                + " 2) List nodes<br>"
                + " 3) Installation<br>"
                + " 4) <font color=\"#f14c1a\"><strong>Configuration</strong></font>");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        Label label = new Label("<strong>Choose directories</strong>");
        label.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label, 2, 0, 5, 0);
        grid.setComponentAlignment(label, Alignment.TOP_CENTER);

        final TextField textFieldDataDir = new TextField("Data Directory:");
        grid.addComponent(textFieldDataDir, 2, 1, 5, 1);
        grid.setComponentAlignment(textFieldDataDir, Alignment.TOP_LEFT);

        final TextField textFieldCommitLogDir = new TextField("Commit Log Directory:");
        grid.addComponent(textFieldCommitLogDir, 2, 2, 5, 2);
        grid.setComponentAlignment(textFieldCommitLogDir, Alignment.TOP_LEFT);

        final TextField textFieldSavedCachesDir = new TextField("Saved Caches Directory:");
        grid.addComponent(textFieldSavedCachesDir, 2, 3, 5, 3);
        grid.setComponentAlignment(textFieldSavedCachesDir, Alignment.TOP_LEFT);

        Button next = new Button("Set directories and Finish (Save)");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                String dataDir = textFieldDataDir.getValue().toString();
                String commitDir = textFieldCommitLogDir.getValue().toString();
                String cacheDir = textFieldSavedCachesDir.getValue().toString();
                if (dataDir.length() > 0 && commitDir.length() > 0 && cacheDir.length() > 0) {
                    cassandraWizard.getCluster().setDataDir(dataDir);
                    cassandraWizard.getCluster().setCommitLogDir(commitDir);
                    cassandraWizard.getCluster().setSavedCacheDir(cacheDir);
                    for (Agent agent : cassandraWizard.getLxcList()) {

                        int reqSeqNumber = cassandraWizard.getTask().getIncrementedReqSeqNumber();
                        UUID taskUuid = cassandraWizard.getTask().getUuid();
                        List<String> args = new ArrayList<String>();
//                    args.add("--force-yes");
//                    args.add("--assume-yes");
//                    args.add("install");
//                    args.add("ksks-cassandra");
                        dataDirCommand = dataDirCommand.replace("%dataDir", dataDir);
                        Command command = buildCommand(agent.getUuid(), dataDirCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);

                        commitDirCommand = commitDirCommand.replace("%commitDir", commitDir);
                        command = buildCommand(agent.getUuid(), commitDirCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);

                        cacheDirCommand = cacheDirCommand.replace("%cacheDir", cacheDir);
                        command = buildCommand(agent.getUuid(), dataDirCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);

                    }
                } else {
                    getWindow().showNotification(
                            "Please fill the form.",
                            Window.Notification.TYPE_TRAY_NOTIFICATION);
                }

                cassandraWizard.showNext();
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraWizard.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

    private Command buildCommand(UUID uuid, String program, int reqSeqNumber, UUID taskUuid, List<String> args) {

        Request request = new Request();
        request.setSource("CassandraModule");
        request.setProgram(program);
        request.setUuid(uuid);
        request.setType(RequestType.EXECUTE_REQUEST);
        request.setTaskUuid(taskUuid);
        request.setWorkingDirectory("/");
        request.setStdOut(OutputRedirection.RETURN);
        request.setStdErr(OutputRedirection.RETURN);
        request.setRunAs("root");
        request.setTimeout(0);
        request.setArgs(args);
        request.setRequestSequenceNumber(reqSeqNumber);
        Command command = new Command(request);

        return command;
    }

}

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
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author bahadyr
 */
public class Step6 extends FormLayout {

    String dataDirCommand = "sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/data_file_directories:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml) + 1`'s!.*!     - %dir!'";
    String commitDirCommand = "sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/commitlog_directory:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!commitlog_directory:%dir!'";

    String cacheDirCommand = "sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/saved_caches_directory:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!saved_caches_directory:%dir!'";

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
                + " 1) <font color=\"#f14c1a\"><strong>Welcome</strong></font><br>"
                + " 2) Install<br>"
                + " 3) Set listen and rpc addresss<br>"
                + " 4) Set seeds<br>"
                + " 5) Set cluster name<br>"
                + " 6) <strong>Set folders</strong><br>"
        );
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        Label label = new Label("<strong>Set directories</strong>");
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

        Button next = new Button("Finish");
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
                        dataDirCommand = dataDirCommand.replace("%dir", dataDir);
                        Command command = buildCommand(agent.getUuid(), dataDirCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);

                        commitDirCommand = commitDirCommand.replace("%dir", commitDir);
                        command = buildCommand(agent.getUuid(), commitDirCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);

                        cacheDirCommand = cacheDirCommand.replace("%dir", cacheDir);
                        command = buildCommand(agent.getUuid(), cacheDirCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);

                    }
                } 

                cassandraWizard.showNext();
            }
        });

        Button back = new Button("Cancel");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraWizard.cancelWizard();
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
        return (Command) CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST,
                uuid,
                CassandraModule.MODULE_NAME,
                taskUuid,
                reqSeqNumber,
                "/",
                program,
                OutputRedirection.RETURN,
                OutputRedirection.RETURN,
                null,
                null,
                "root",
                args,
                null);
    }

}

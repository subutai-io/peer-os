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
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author bahadyr
 */
public class Step5 extends Panel {

    String changeNameCommand = "sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/cluster_name:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!cluster_name: \"%newName\"!'";

    public Step5(final CassandraWizard cassandraWizard) {
        setCaption("Rename cluster");
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
                + " 4) <font color=\"#f14c1a\"><strong>Configuration</strong></font><br>");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        final TextField clusterName = new TextField("Name your Cluster:");

        grid.addComponent(clusterName, 2, 0, 5, 1);
        grid.setComponentAlignment(clusterName, Alignment.MIDDLE_CENTER);

        Button next = new Button("Rename cluster");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Agent agent : cassandraWizard.getLxcList()) {
                    if (clusterName.getValue().toString().length() > 0) {
                        int reqSeqNumber = cassandraWizard.getTask().getIncrementedReqSeqNumber();
                        UUID taskUuid = cassandraWizard.getTask().getUuid();
                        List<String> args = new ArrayList<String>();
                        changeNameCommand = changeNameCommand.replace("%newName", clusterName.getValue().toString());
                        Command command = buildCommand(agent.getUuid(), changeNameCommand, reqSeqNumber, taskUuid, args);
                        cassandraWizard.runCommand(command);
                        cassandraWizard.getCluster().setName(clusterName.getValue().toString());
                    }
                }
                cassandraWizard.showNext();
            }
        });
        Button back = new Button("Back");
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

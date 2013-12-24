/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;

/**
 * @author bahadyr
 */
public class Step2 extends Panel {

    String installationCommand = "apt-get --force-yes --assume-yes install ksks-cassandra";
    String purgeCommand = "apt-get --force-yes --assume-yes purge ksks-cassandra";

    public Step2(final CassandraWizard cassandraWizard) {
//        this.cassandraWizard = cassandraWizard;

        setCaption("Select nodes");
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
                + " 2) <strong>Install</strong><br>"
                + " 3) Set listen and rpc addresss<br>"
                + " 4) Set seeds<br>"
                + " 5) Set cluster name<br>"
                + " 6) Set folders<br>"
        );
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        Label label = new Label("Target hosts");
        label.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label, 2, 0, 5, 0);
        grid.setComponentAlignment(label, Alignment.TOP_CENTER);

        StringBuilder sb = new StringBuilder();
        for (Agent a : MgmtApplication.getSelectedAgents()) {
            sb.append(a.getHostname()).append("<br/>");
        }
        Label label1 = new Label(sb.toString());
        label1.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label1, 2, 1, 5, 1);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Agent agent : MgmtApplication.getSelectedAgents()) {
                    int reqSeqNumber = cassandraWizard.getTask().getIncrementedReqSeqNumber();
                    UUID taskUuid = cassandraWizard.getTask().getUuid();
                    List<String> args = new ArrayList<String>();
                    Command command = buildCommand(agent.getUuid(), installationCommand, reqSeqNumber, taskUuid, args);
                    cassandraWizard.runCommand(command);

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
                null,
                null);
    }

}

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
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;

/**
 * @author bahadyr
 */
public class Step3 extends Panel {

    String listenAddressCommand = "sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/listen_address:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!listen_address: %ip!'";
    String rpcAddressCommand = "sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/rpc_address:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!rpc_address: %ip!'";

    String purgeCommand = "apt-get --force-yes --assume-yes purge ksks-cassandra";

    public Step3(final CassandraWizard cassandraWizard) {
        setCaption("Configure listen address/prc address");
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
                + " 1) <font color=\"#f14c1a\">Welcome</font><br>"
                + " 2) Install<br>"
                + " 3) <strong>Set listen and rpc addresss</strong><br>"
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
        for (Agent a : cassandraWizard.getLxcList()) {
            sb.append(a.getHostname()).append("<br/>");
        }
        Label label1 = new Label(sb.toString());
        label1.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label1, 2, 1, 5, 1);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);

        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Agent agent : cassandraWizard.getLxcList()) {
                    int reqSeqNumber = cassandraWizard.getTask().getIncrementedReqSeqNumber();
                    UUID taskUuid = cassandraWizard.getTask().getUuid();
                    List<String> args = new ArrayList<String>();
                    listenAddressCommand = listenAddressCommand.replace("%ip", agent.getHostname());
                    Command command = buildCommand(agent.getUuid(), listenAddressCommand, reqSeqNumber, taskUuid, args);
                    cassandraWizard.runCommand(command);
                    rpcAddressCommand = rpcAddressCommand.replace("%ip", agent.getHostname());
                    command = buildCommand(agent.getUuid(), rpcAddressCommand, reqSeqNumber, taskUuid, args);
                    cassandraWizard.runCommand(command);
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

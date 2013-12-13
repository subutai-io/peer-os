/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class Step2 extends Panel {

    //    private static final List<String> hosts = Arrays.asList(new String[]{
//            "cassandra-node1", "cassandra-node2", "cassandra-node3", "cassandra-node4", "cassandra-node5"});
    List<Agent> hosts;
    CassandraWizard parent;
    String installationCommand = "apt-get --force-yes --assume-yes install ksks-cassandra";
//    String listenAddressCommand = "sed -i 's/listen_address: localhost/listen_address: %ip/g' /opt/cassandra-2.0.0/conf/cassandra.yaml";
//    String rpcAddressCommand = "sed -i 's/rpc_address: localhost/rpc_address: %ip/g' /opt/cassandra-2.0.0/conf/cassandra.yaml";

    String purgeCommand = "apt-get --force-yes --assume-yes purge ksks-cassandra";

    public Step2(final CassandraWizard cassandraWizard) {
        parent = cassandraWizard;

        setCaption("List nodes Step2");
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
                + " 2) <font color=\"#f14c1a\"><strong>List nodes</strong></font><br>"
                + " 3) Installation<br>"
                + " 4) Configuration<br>");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        Label label = new Label("Please enter the list of hosts to be included in the cluster");
        label.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label, 2, 0, 5, 0);
        grid.setComponentAlignment(label, Alignment.TOP_CENTER);

        Label label1 = new Label("<strong>Target Hosts</strong><br>"
                + "<br>");
        label1.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label1, 2, 1, 5, 1);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);

        // 'Shorthand' constructor - also supports data binding using Containers
        hosts = new ArrayList<Agent>(AppData.getSelectedAgentList());
//        List<UUID> agentUuids = new ArrayList<UUID>();
//        for (Agent agent : hosts) {
//            agentUuids.add(agent.getUuid());
//        }
//        cassandraWizard.getCluster().setNodes(hosts);
        final ListSelect hostSelect = new ListSelect("Enter a list of hosts using Fully Qualified Domain Name or IP", hosts);

        hostSelect.setRows(6); // perfect length in out case
        hostSelect.setNullSelectionAllowed(true); // user can not 'unselect'
        hostSelect.setMultiSelect(true);
        hostSelect.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                getWindow().showNotification("hosts selected");
                cassandraWizard.step4.hosts = hosts;
            }
        });

        grid.addComponent(hostSelect, 2, 2, 5, 9);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);

        Button next = new Button("Install");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for(Iterator i = hostSelect.getItemIds().iterator(); i.hasNext();) {
                    Agent agent = (Agent) i.next();
                    int reqSeqNumber = cassandraWizard.getTask().getIncrementedReqSeqNumber();
                    UUID taskUuid = cassandraWizard.getTask().getUuid();
                    List<String> args = new ArrayList<String>();
//                    args.add("--force-yes");
//                    args.add("--assume-yes");
//                    args.add("install");
//                    args.add("ksks-cassandra");
                    Command command = buildCommand(agent.getUuid(), installationCommand, reqSeqNumber, taskUuid, args);
                    cassandraWizard.runCommand(command);
                    
                }
//                for (Agent agent : hosts) {
////                    listenAddressCommand = listenAddressCommand.replace("%ip", agent.getHostname());
////                    command = buildCommand(agent.getUuid(), listenAddressCommand, reqSeqNumber, taskUuid, args);
////                    cassandraWizard.runCommand(command);
////                    rpcAddressCommand = rpcAddressCommand.replace("%ip", agent.getHostname());
////                    command = buildCommand(agent.getUuid(), rpcAddressCommand, reqSeqNumber, taskUuid, args);
////                    cassandraWizard.runCommand(command);
//                }
                cassandraWizard.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Agent agent : hosts) {
                    int reqSeqNumber = cassandraWizard.getTask().getIncrementedReqSeqNumber();
                    UUID taskUuid = cassandraWizard.getTask().getUuid();
                    List<String> args = new ArrayList<String>();
//                    args.add("--force-yes");
//                    args.add("--assume-yes");
//                    args.add("purge");
//                    args.add("ksks-cassandra");
                    Command command = buildCommand(agent.getUuid(), purgeCommand, reqSeqNumber, taskUuid, args);
                    cassandraWizard.runCommand(command);
                }
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

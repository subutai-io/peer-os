/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class Step2 extends Panel {

    private static final List<String> hosts = Arrays.asList(new String[]{
            "hadoop-node1", "hadoop-node2", "hadoop-node3", "hadoop-node4", "hadoop-node5"});
    //List<String> hosts;
    HadoopWizard parent;
    String installationCommand = "apt-get --force-yes --assume-yes install ksks-zookeeper";
    String purgeCommand = "apt-get --force-yes --assume-yes purge ksks-zookeeper";

    public Step2(final HadoopWizard hadoopWizard) {
        parent = hadoopWizard;

        setCaption("Welcome to Hadoop Cluster Installation");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) Master Configurations<br>"
                + " 2) <font color=\"#f14c1a\"><strong>Slave Configurations</strong></font><br>");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 0, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        Label label = new Label("<strong>Enter a list of hosts that will run as Data nodes.<br>" +
                "(Provide at least 1)</strong>");
        label.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(label);

        TwinColSelect twinColSelectDataNodes = new TwinColSelect();
        for (String host : hosts) {
            twinColSelectDataNodes.addItem(host);
        }
        twinColSelectDataNodes.setRows(10);
        twinColSelectDataNodes.setNullSelectionAllowed(true);
        twinColSelectDataNodes.setMultiSelect(true);
        twinColSelectDataNodes.setImmediate(true);
        twinColSelectDataNodes.setLeftColumnCaption("Available Nodes");
        twinColSelectDataNodes.setRightColumnCaption("Data Nodes");
        twinColSelectDataNodes.setWidth("350px");
        verticalLayoutForm.addComponent(twinColSelectDataNodes);

        Label labelTaskTrackerCaption = new Label("<strong>Enter a list of hosts that will run as Task tracker.<br>" +
                "(Provide at least 1)</strong>");
        labelTaskTrackerCaption.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(labelTaskTrackerCaption);

        TwinColSelect twinColSelectTaskTrackers = new TwinColSelect();
        for (String host : hosts) {
            twinColSelectTaskTrackers.addItem(host);
        }
        twinColSelectTaskTrackers.setRows(10);
        twinColSelectTaskTrackers.setNullSelectionAllowed(true);
        twinColSelectTaskTrackers.setMultiSelect(true);
        twinColSelectTaskTrackers.setImmediate(true);
        twinColSelectTaskTrackers.setLeftColumnCaption("Available Nodes");
        twinColSelectTaskTrackers.setRightColumnCaption("Task Trackers");
        twinColSelectTaskTrackers.setWidth("350px");
        verticalLayoutForm.addComponent(twinColSelectTaskTrackers);

        grid.addComponent(verticalLayoutForm, 1, 0, 5, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.MIDDLE_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                parent.showNext();
            }
        });
        Button back = new Button("Finish");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                parent.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

    private Command buildCommand(UUID uuid, String program, Integer reqSeqNumber, UUID taskUuid) {

        Request request = new Request();
        request.setSource("HadoopModule Wizard");
        request.setProgram(program);
        request.setUuid(uuid);
        request.setType(RequestType.EXECUTE_REQUEST);
        request.setTaskUuid(taskUuid);
        request.setWorkingDirectory("/");
        request.setStdOut(OutputRedirection.RETURN);
        request.setStdErr(OutputRedirection.RETURN);
        request.setRunAs("root");
        request.setTimeout(0);
        request.setRequestSequenceNumber(reqSeqNumber);
        Command command = new Command(request);

        return command;
    }

}

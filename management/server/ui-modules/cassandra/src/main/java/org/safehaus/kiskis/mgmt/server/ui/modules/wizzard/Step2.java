/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 * @author bahadyr
 */
public class Step2 extends Panel {

//    private static final List<String> hosts = Arrays.asList(new String[]{
//            "cassandra-node1", "cassandra-node2", "cassandra-node3", "cassandra-node4", "cassandra-node5"});
    List<String> hosts;

    public Step2(final CassandraWizard aThis) {
        setCaption("List nodes");
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
        hosts = new ArrayList<String>(AppData.getSelectedAgentList());
        ListSelect hostSelect = new ListSelect("Enter a list of hosts using Fully Qualified Domain Name or IP", hosts);

        hostSelect.setRows(10); // perfect length in out case
        hostSelect.setNullSelectionAllowed(true); // user can not 'unselect'

        grid.addComponent(hostSelect, 2, 2, 5, 9);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);

        Button next = new Button("Install");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (String uuid : hosts) {
                    Command command = buildCommand(uuid);
                    aThis.runCommand(command);
                }
                aThis.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                aThis.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

    private Command buildCommand(String uuid) {
        Request request = new Request();
        request.setProgram("ls");
        request.setUuid(uuid);
        request.setType(RequestType.EXECUTE_REQUEST);
        request.setTaskUuid("someuuid");
        request.setWorkingDirectory("/");
        request.setStdOut(OutputRedirection.RETURN);
        request.setStdErr(OutputRedirection.RETURN);
        request.setRunAs("root");
        request.setTimeout(60l);
        request.setRequestSequenceNumber(1l);
        Command command = new Command(request);
        return command;
    }

}

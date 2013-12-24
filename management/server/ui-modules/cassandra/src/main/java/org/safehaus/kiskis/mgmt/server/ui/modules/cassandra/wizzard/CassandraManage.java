/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import static org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule.getCommandManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.component.CassandraTable;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author bahadyr
 */
public class CassandraManage {

    private final VerticalLayout verticalLayout;
    private Task task;
//    private CassandraClusterInfo cluster;
//    private final List<Agent> lxcList;
//    private final TextArea terminal;
//    private final ProgressIndicator progressBar;
    private final Button getClusters;
    private final CassandraTable cassandraTable;

    /**
     *
     * @param cm
     */
    public CassandraManage() {
        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        // Create table
        cassandraTable = new CassandraTable(getCommandManager());
        cassandraTable.setPageLength(6);

        verticalLayout.addComponent(cassandraTable);

//            buttonInstallWizard = new Button("Cassandra Cluster Installation Wizard");
//            buttonInstallWizard.addListener(new Button.ClickListener() {
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//                    if (getLxcAgents().size() > 0) {
//                        cassandraWizard = new CassandraWizard(getLxcAgents());
//                        getApplication().getMainWindow().addWindow(cassandraWizard);
//                    }
//                }
//
//                
//            });
//        verticalLayout.addComponent(buttonInstallWizard);
        getClusters = new Button("Get Cassandra clusters");
        getClusters.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraTable.refreshDatasource();
            }
        });
//
        verticalLayout.addComponent(getClusters);
        verticalLayout.addComponent(cassandraTable);
//            terminal = new TextArea();
//            terminal.setRows(10);
//            terminal.setColumns(65);
//            terminal.setImmediate(true);
//            terminal.setWordwrap(true);
//            verticalLayout.addComponent(terminal);
    }

    public Component getContent() {
        return verticalLayout;
    }

}

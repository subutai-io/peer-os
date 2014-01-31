/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class StepStart extends Panel {

    public StepStart(final CassandraWizard wizard) {
        setSizeFull();

        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Cassandra Installation Wizard!</h2><br/>"
                + "Please select nodes in the tree on the left to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label(
                String.format("<img src='http://%s:%s/cassandra-logo.png' width='150px'/>", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        logoImg.setHeight(150, Sizeable.UNITS_PIXELS);
        logoImg.setWidth(220, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Start");
        next.setWidth(100, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(next, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        next.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<Agent> selectedAgents = MgmtApplication.getSelectedAgents();
                if (Util.isCollectionEmpty(selectedAgents)) {
                    show("Select nodes in the tree on the left first");
                } else {
                    wizard.getConfig().reset();
                    wizard.getConfig().setSelectedAgents(selectedAgents);
                    wizard.next();
                }
            }
        });

        addComponent(gridLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

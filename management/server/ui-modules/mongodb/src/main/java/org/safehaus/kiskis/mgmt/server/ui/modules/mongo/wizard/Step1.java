/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

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
public class Step1 extends Panel {

//    private final MongoWizard mongoWizard;
    public Step1(final MongoWizard mongoWizard) {
//        this.mongoWizard = mongoWizard;

        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Mongo Installation Wizard!</h2><br/>"
                + "Please select nodes in the tree on the left to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label(
                String.format("<img src='http://%s:%s/mongodb-logo.png' width='150px'/>", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Next");
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
                    mongoWizard.getConfig().setSelectedAgents(selectedAgents);
                    mongoWizard.next();
                }
            }
        });

        addComponent(gridLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

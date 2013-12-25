/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class StepFinish extends Panel {

    public StepFinish(final CassandraWizard wizard) {

        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Cassandra Installation Wizard Complete!</h2>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Button finish = new Button("Finish");
        finish.setWidth(100, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(finish, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(finish, Alignment.BOTTOM_RIGHT);

        finish.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.next();
            }
        });

        addComponent(gridLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

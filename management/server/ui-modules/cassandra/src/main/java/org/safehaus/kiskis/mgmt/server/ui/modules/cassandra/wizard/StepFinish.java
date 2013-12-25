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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.Installer;

/**
 *
 * @author dilshat
 */
public class StepFinish extends Panel {

    private TextArea terminal;

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
                Installer installer = new Installer(wizard.getConfig(), terminal);
                wizard.registerResponseListener(installer);
                installer.start();
            }
        });
        terminal = new TextArea();
        terminal.setRows(10);
        terminal.setColumns(50);
        gridLayout.addComponent(terminal);
        addComponent(gridLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

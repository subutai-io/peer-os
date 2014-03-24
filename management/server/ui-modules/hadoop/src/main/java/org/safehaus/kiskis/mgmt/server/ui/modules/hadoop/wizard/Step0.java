/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;

/**
 * @author dilshat
 */
public class Step0 extends Panel {

    public Step0(final HadoopWizard hadoopWizard) {

        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Hadoop Installation Wizard!</h2><br/>"
                + "Please select nodes in the tree on the left to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/hadoop.jpg"));
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
                List<Agent> list = hadoopWizard.getLxcList();
                if (list.isEmpty() || list == null) {
                    show("Select nodes in the tree on the left first");
                } else {
                    hadoopWizard.showNext();
                }
            }
        });

        addComponent(gridLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

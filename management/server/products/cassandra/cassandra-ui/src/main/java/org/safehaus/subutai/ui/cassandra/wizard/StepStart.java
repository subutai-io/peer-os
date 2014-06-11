/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.cassandra.wizard;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

/**
 * @author dilshat
 */
public class StepStart extends VerticalLayout {

    public StepStart(final Wizard wizard) {
        setSizeFull();

        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Cassandra Installation Wizard!</h2><br/>"
                        + "Please select nodes in the tree on the left to continue</center>"
        );
        welcomeMsg.setContentMode(ContentMode.HTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/cassandra.png"));
        logoImg.setContentMode(ContentMode.HTML);
        logoImg.setHeight(150, Unit.PIXELS);
        logoImg.setWidth(220, Unit.PIXELS);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Start");
        next.setWidth(100, Unit.PIXELS);
        gridLayout.addComponent(next, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        next.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                wizard.getConfig().reset();
                wizard.next();
            }
        });

        addComponent(gridLayout);
    }

    private void show(String notification) {
        Notification.show(notification);
    }

}

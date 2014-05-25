/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.cassandra.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

/**
 * @author dilshat
 */
public class WelcomeStep extends Panel {

    public WelcomeStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(10, 6);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label welcomeMsg = new Label("<center><h2>Welcome to Cassandra Installation Wizard!</h2>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        grid.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/cassandra.png"));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        logoImg.setHeight(150, Sizeable.UNITS_PIXELS);
        logoImg.setWidth(150, Sizeable.UNITS_PIXELS);
        grid.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Start");
        next.setWidth(100, Sizeable.UNITS_PIXELS);
        grid.addComponent(next, 6, 4, 6, 4);
        grid.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        next.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                wizard.init();
                wizard.next();
            }
        });

        addComponent(grid);
    }

}

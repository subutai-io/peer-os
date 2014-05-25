/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.zookeeper.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.zookeeper.Config;

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

        Label welcomeMsg = new Label(String.format("<center><h2>Welcome to %s Installation Wizard!</h2>", Config.PRODUCT_KEY));
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        grid.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/zk.jpg"));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        logoImg.setHeight(204, Sizeable.UNITS_PIXELS);
        logoImg.setWidth(150, Sizeable.UNITS_PIXELS);
        grid.addComponent(logoImg, 1, 3, 2, 5);

        Button startStandalone = new Button("Start standalone installation");
//        startStandalone.setWidth(100, Sizeable.UNITS_PIXELS);
        grid.addComponent(startStandalone, 6, 4, 6, 4);
        grid.setComponentAlignment(startStandalone, Alignment.BOTTOM_RIGHT);
        Button startOverHadoop = new Button("Start over-Hadoop installation");
//        startOverHadoop.setWidth(100, Sizeable.UNITS_PIXELS);
        grid.addComponent(startOverHadoop, 7, 4, 7, 4);
        grid.setComponentAlignment(startOverHadoop, Alignment.BOTTOM_RIGHT);

        startStandalone.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
                wizard.getConfig().setStandalone(true);
                wizard.next();
            }
        });
        startOverHadoop.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
                wizard.next();
            }
        });

        addComponent(grid);
    }

}

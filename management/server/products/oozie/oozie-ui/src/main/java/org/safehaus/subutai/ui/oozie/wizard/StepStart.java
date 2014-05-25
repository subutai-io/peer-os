/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import java.util.logging.Logger;

/**
 * @author dilshat
 */
public class StepStart extends Panel {

    private static final Logger LOG = Logger.getLogger(StepStart.class.getName());

    VerticalLayout vLayout;

    public StepStart(final Wizard wizard) {
        setSizeFull();
        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Oozie Installation Wizard!</h2><br/>"
                        + "Please click Start button to continue</center>"
        );
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/oozie.png"));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        logoImg.setHeight(150, Sizeable.UNITS_PIXELS);
        logoImg.setWidth(220, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        HorizontalLayout hl = new HorizontalLayout();

        Button next = new Button("Start");
        next.setWidth(100, Sizeable.UNITS_PIXELS);
        next.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                wizard.next();
            }
        });

        hl.addComponent(next);

        gridLayout.addComponent(hl, 6, 4, 6, 4);
        addComponent(gridLayout);
        vLayout = new VerticalLayout();
        addComponent(vLayout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

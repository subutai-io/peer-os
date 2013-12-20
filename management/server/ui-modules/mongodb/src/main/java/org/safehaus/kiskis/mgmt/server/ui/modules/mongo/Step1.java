/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule.ModuleComponent;

/**
 *
 * @author dilshat
 */
public class Step1 extends Panel {

    ModuleComponent mongo;

    public Step1(final ModuleComponent mongo) {
        this.mongo = mongo;

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Config Servers and Routers</strong></font><br>"
                + " 2) Replica Set Configurations");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayout.setSpacing(true);

        final TextField textFieldClusterName = new TextField("Enter your cluster name");
        textFieldClusterName.setInputPrompt("Cluster name");
        textFieldClusterName.setRequired(true);
        textFieldClusterName.setRequiredError("Must have a name");
        verticalLayoutForm.addComponent(textFieldClusterName);
        
        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);        

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                mongo.showNext();
            }
        });

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(next);

        addComponent(verticalLayout);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;

/**
 *
 * @author dilshat
 */
public class Step1 extends Panel {

    private final MongoWizard mongoWizard;

    public Step1(final MongoWizard mongoWizard) {
        this.mongoWizard = mongoWizard;

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        Label menu = new Label("<center><h2>Welcome to Mongo Installation Wizard</h2></center>" + 
                "<img src='http://localhost:8888/mongodb-logo.png'/>");
        menu.setContentMode(Label.CONTENT_XHTML);
        verticalLayout.addComponent(menu);
//        System.out.println(MgmtApplication.getInstance());
//        if (MgmtApplication.getInstance() != null) {
//            FileResource logo = new FileResource(new File(System.getProperty("karaf.base") + "/mongodb-logo.png"), null);
//            System.out.println(logo);
//            Embedded logoEmbedded = new Embedded("MongoDb", logo);
//            verticalLayout.addComponent(logoEmbedded);
//        }

        addComponent(verticalLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}

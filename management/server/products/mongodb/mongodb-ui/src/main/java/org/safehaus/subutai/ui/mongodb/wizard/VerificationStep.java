/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.mongodb.wizard;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.ui.mongodb.MongoUI;
import org.safehaus.subutai.ui.mongodb.components.ProgressWindow;

import java.util.UUID;

/**
 * @author dilshat
 */
public class VerificationStep extends VerticalLayout {

    public VerificationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(1, 5);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label confirmationLbl = new Label("<strong>Please verify the installation settings "
                + "(you may change them by clicking on Back button)</strong><br/>");
        confirmationLbl.setContentMode(ContentMode.HTML);

        ConfigView cfgView = new ConfigView("Installation configuration");
        cfgView.addStringCfg("Cluster Name", wizard.getConfig().getClusterName());
        cfgView.addStringCfg("Replica Set Name", wizard.getConfig().getReplicaSetName());
        cfgView.addStringCfg("Domain Name", wizard.getConfig().getDomainName());
        cfgView.addStringCfg("Number of configuration servers", wizard.getConfig().getNumberOfConfigServers() + "");
        cfgView.addStringCfg("Number of routers", wizard.getConfig().getNumberOfRouters() + "");
        cfgView.addStringCfg("Number of data nodes", wizard.getConfig().getNumberOfDataNodes() + "");
        cfgView.addStringCfg("Configuration servers port", wizard.getConfig().getCfgSrvPort() + "");
        cfgView.addStringCfg("Routers port", wizard.getConfig().getRouterPort() + "");
        cfgView.addStringCfg("Data nodes port", wizard.getConfig().getDataNodePort() + "");

        Button install = new Button("Install");
        install.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                UUID trackID = MongoUI.getMongoManager().installCluster(wizard.getConfig());
//                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, null);
                ProgressWindow window = new ProgressWindow(MongoUI.getTracker(), trackID, Config.PRODUCT_KEY);
                getUI().addWindow(window.getWindow());
                wizard.init();
            }
        });

        Button back = new Button("Back");
        back.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                wizard.back();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(install);

        grid.addComponent(confirmationLbl, 0, 0);

        grid.addComponent(cfgView.getCfgTable(), 0, 1, 0, 3);

        grid.addComponent(buttons, 0, 4);

        addComponent(grid);

    }

}

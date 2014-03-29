/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.wizard;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class VerificationStep extends Panel {

    public VerificationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(1, 5);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label confirmationLbl = new Label("<strong>Please verify the installation settings "
                + "(you may change them by clicking on Back button)</strong><br/>");
        confirmationLbl.setContentMode(Label.CONTENT_XHTML);

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
        install.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                UUID trackID = MongoUI.getMongoManager().installCluster(wizard.getConfig());
                MongoUI.showProgressWindow(trackID, null);
                wizard.init();
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
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

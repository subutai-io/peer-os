/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ConfigView;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class VerifyStep extends Panel {

    private static final Logger LOG = Logger.getLogger(VerifyStep.class.getName());

    public VerifyStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(1, 5);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label confirmationLbl = new Label("<strong>Please verify the installation configuration "
                + "(you may change it by clicking on Back button)</strong><br/>");
        confirmationLbl.setContentMode(Label.CONTENT_XHTML);

        ConfigView cfgView = new ConfigView("Installation configuration");
        cfgView.addStringCfg("Cluster Name", wizard.getConfig().getClusterName());
        cfgView.addStringCfg("Replica Set Name", wizard.getConfig().getReplicaSetName());
        cfgView.addAgentSetCfg("Configuration servers", wizard.getConfig().getConfigServers());
        cfgView.addAgentSetCfg("Routers", wizard.getConfig().getRouterServers());
        cfgView.addAgentSetCfg("Data Nodes", wizard.getConfig().getDataNodes());

        Button install = new Button("Install");
        install.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //check once again
                if (Util.isCollectionEmpty(wizard.getConfig().getConfigServers())) {
                    show("Please add config servers");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getRouterServers())) {
                    show("Please add routers");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getDataNodes())) {
                    show("Please add data nodes");
                } else if (wizard.getConfig().getConfigServers().size() != 1
                        && wizard.getConfig().getConfigServers().size() != 3) {
                    show("Please, select 1 or 3 nodes as config servers");
                } else if (wizard.getConfig().getDataNodes().size() % 2 == 0) {
                    show("Please add odd number of data nodes");
                } else if (wizard.getConfig().getDataNodes().size() > 7) {
                    show("Please add no more than 7 data nodes");
                } else {
                    MongoClusterInfo mongoClusterInfo
                            = new MongoClusterInfo(
                                    wizard.getConfig().getClusterName(),
                                    wizard.getConfig().getReplicaSetName(),
                                    wizard.getConfig().getConfigServers(),
                                    wizard.getConfig().getRouterServers(),
                                    wizard.getConfig().getDataNodes());
                    if (MongoDAO.saveMongoClusterInfo(mongoClusterInfo)) {
                        wizard.next();
                    } else {
                        show("Could not save new cluster configuration! Please see logs.");
                    }
                }
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

    private void show(String notification) {
        getWindow().showNotification(notification);
    }
}

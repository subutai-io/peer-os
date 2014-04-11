package org.safehaus.kiskis.mgmt.ui.flume.wizard;

import com.vaadin.ui.*;

import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.ui.flume.FlumeUI;

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
        for (Agent agent : wizard.getConfig().getNodes()) {
            cfgView.addStringCfg("Node to install", agent.getHostname() + "");
        }

        Button install = new Button("Install");
        install.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                UUID trackID = FlumeUI.getManager().installCluster(wizard.getConfig());
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, null);
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

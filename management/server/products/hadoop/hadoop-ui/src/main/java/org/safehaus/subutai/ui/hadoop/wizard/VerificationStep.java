/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.hadoop.wizard;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

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
		cfgView.addStringCfg("Domain Name", wizard.getConfig().getDomainName());
		cfgView.addStringCfg("Number of slave nodes", wizard.getConfig().getCountOfSlaveNodes() + "");
		cfgView.addStringCfg("Replication factor", wizard.getConfig().getReplicationFactor() + "");

		Button install = new Button("Install");
		install.addStyleName("default");
		install.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				UUID trackID = HadoopUI.getHadoopManager().installCluster(wizard.getConfig());
				ProgressWindow window = new ProgressWindow(HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID, HadoopClusterConfig.PRODUCT_KEY);
				window.getWindow().addCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(Window.CloseEvent closeEvent) {
						wizard.init();
					}
				});
				getUI().addWindow(window.getWindow());
			}
		});

		Button back = new Button("Back");
		back.addStyleName("default");
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

package org.safehaus.subutai.plugin.flume.ui.wizard;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import java.util.UUID;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.plugin.flume.ui.FlumeUI;

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
		for (Agent agent : wizard.getConfig().getNodes()) {
			cfgView.addStringCfg("Node to install", agent.getHostname() + "");
		}

		Button install = new Button("Install");
		install.addStyleName("default");
		install.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				UUID trackID = FlumeUI.getManager().installCluster(wizard.getConfig());
                ProgressWindow window = new ProgressWindow(FlumeUI.getExecutor(), FlumeUI.getTracker(), trackID, FlumeConfig.PRODUCT_KEY);
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

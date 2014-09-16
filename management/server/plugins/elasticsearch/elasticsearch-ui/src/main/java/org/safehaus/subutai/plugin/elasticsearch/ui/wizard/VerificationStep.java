package org.safehaus.subutai.plugin.elasticsearch.ui.wizard;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.plugin.elasticsearch.api.*;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.plugin.elasticsearch.ui.ElasticsearchUI;

import java.util.UUID;

public class VerificationStep extends VerticalLayout {

	public VerificationStep(final Wizard wizard) {

		setSizeFull();

		GridLayout grid = new GridLayout(1, 5);
		grid.setSpacing(true);
		grid.setMargin(true);
		grid.setSizeFull();

		Label confirmationLbl = new Label("<strong>Please verify the installation settings "
				+ "(You may change them by clicking on Back button)</strong><br/>");
		confirmationLbl.setContentMode(ContentMode.HTML);

		ConfigView cfgView = new ConfigView("Installation configuration");
		cfgView.addStringCfg("Cluster Name: ", wizard.getElasticsearchClusterConfiguration().getClusterName());
		cfgView.addStringCfg("Number of Nodes: ", "" + wizard.getElasticsearchClusterConfiguration().getNumberOfNodes());
		cfgView.addStringCfg("Number of Master Nodes: ", "" + wizard.getElasticsearchClusterConfiguration().getNumberOfMasterNodes());
		cfgView.addStringCfg("Number of Data Nodes: ", "" + wizard.getElasticsearchClusterConfiguration().getNumberOfDataNodes());
		cfgView.addStringCfg("Number of Shards: ", "" + wizard.getElasticsearchClusterConfiguration().getNumberOfShards());
		cfgView.addStringCfg("Number of Replicas: ", "" + wizard.getElasticsearchClusterConfiguration().getNumberOfReplicas());

		Button installButton = new Button("Install");
		installButton.addStyleName("default");
		installButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {

				UUID trackID = ElasticsearchUI.getElasticsearchManager().installCluster(wizard.getElasticsearchClusterConfiguration());

				ProgressWindow window =
						new ProgressWindow(ElasticsearchUI.getExecutor(), ElasticsearchUI.getTracker(), trackID,
								ElasticsearchClusterConfiguration.PRODUCT_KEY);

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
		buttons.addComponent(installButton);

		grid.addComponent(confirmationLbl, 0, 0);

		grid.addComponent(cfgView.getCfgTable(), 0, 1, 0, 3);

		grid.addComponent(buttons, 0, 4);

		addComponent(grid);

	}

}

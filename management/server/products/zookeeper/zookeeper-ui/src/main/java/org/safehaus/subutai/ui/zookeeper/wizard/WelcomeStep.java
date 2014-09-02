/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.zookeeper.wizard;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.ui.zookeeper.ZookeeperUI;

/**
 * @author dilshat
 */
public class WelcomeStep extends Panel {

	public WelcomeStep(final Wizard wizard) {

		setSizeFull();

		GridLayout grid = new GridLayout(10, 6);
		grid.setSpacing(true);
		grid.setMargin(true);
		grid.setSizeFull();

		Label welcomeMsg = new Label(String.format("<center><h2>Welcome to %s Installation Wizard!</h2>", Config.PRODUCT_KEY));
		welcomeMsg.setContentMode(ContentMode.HTML);
		grid.addComponent(welcomeMsg, 3, 1, 6, 2);

		Label logoImg = new Label();
		logoImg.setIcon(new FileResource(FileUtil.getFile(ZookeeperUI.MODULE_IMAGE, this)));
		logoImg.setContentMode(ContentMode.HTML);
		logoImg.setHeight(204, Unit.PIXELS);
		logoImg.setWidth(150, Unit.PIXELS);
		grid.addComponent(logoImg, 1, 3, 2, 5);

		Button startStandalone = new Button("Start standalone installation");
		startStandalone.addStyleName("default");
		grid.addComponent(startStandalone, 6, 4, 6, 4);
		grid.setComponentAlignment(startStandalone, Alignment.BOTTOM_RIGHT);
		Button startOverHadoop = new Button("Start over-Hadoop installation");
		startOverHadoop.addStyleName("default");
		grid.addComponent(startOverHadoop, 7, 4, 7, 4);
		grid.setComponentAlignment(startOverHadoop, Alignment.BOTTOM_RIGHT);

		startStandalone.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				wizard.init();
				wizard.getConfig().setStandalone(true);
				wizard.next();
			}
		});
		startOverHadoop.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				wizard.init();
				wizard.next();
			}
		});

		setContent(grid);
	}
}

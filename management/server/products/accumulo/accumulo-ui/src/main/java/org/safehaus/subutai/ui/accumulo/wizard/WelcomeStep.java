/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.accumulo.wizard;


import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.ui.accumulo.FileUtil;

import java.io.IOException;


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

		Label welcomeMsg = new Label("<center><h2>Welcome to Accumulo Installation Wizard!</h2>");
		welcomeMsg.setContentMode(ContentMode.HTML);
		grid.addComponent(welcomeMsg, 3, 1, 6, 2);

//		System.out.println(FileUtil.getContent("img/logo.png"));

		Label logoImg = new Label();
		try {
			logoImg.setIcon(new FileResource(FileUtil.readFile("img/logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		logoImg.setContentMode(ContentMode.HTML);
		logoImg.setHeight(56, Unit.PIXELS);
		logoImg.setWidth(220, Unit.PIXELS);
		grid.addComponent(logoImg, 1, 3, 2, 5);

		Button next = new Button("Start");
		next.setWidth(100, Unit.PIXELS);
		grid.addComponent(next, 6, 4, 6, 4);
		grid.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				wizard.init();
				wizard.next();
			}
		});

		setContent(grid);
	}
}

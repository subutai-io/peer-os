/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.wizard;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.FileUtil;
import org.safehaus.subutai.ui.oozie.OozieUI;

import java.util.logging.Logger;

/**
 * @author dilshat
 */
public class StepStart extends Panel {

	private static final Logger LOG = Logger.getLogger(StepStart.class.getName());

	public StepStart(final Wizard wizard) {
		setSizeFull();
		GridLayout gridLayout = new GridLayout(10, 6);
		gridLayout.setSizeFull();

		Label welcomeMsg = new Label(
				"<center><h2>Welcome to Oozie Installation Wizard!</h2><br/>"
						+ "Please click Start button to continue</center>"
		);
		welcomeMsg.setContentMode(ContentMode.HTML);
		gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

		Label logoImg = new Label();
		logoImg.setIcon(new FileResource(FileUtil.getFile(OozieUI.MODULE_IMAGE, this)));
		logoImg.setContentMode(ContentMode.HTML);
		logoImg.setHeight(150, Unit.PIXELS);
		logoImg.setWidth(220, Unit.PIXELS);
		gridLayout.addComponent(logoImg, 1, 3, 2, 5);

		HorizontalLayout hl = new HorizontalLayout();

		Button next = new Button("Start");
		next.addStyleName("default");
		next.setWidth(100, Unit.PIXELS);
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.next();
			}
		});

		hl.addComponent(next);

		gridLayout.addComponent(hl, 6, 4, 6, 4);
		setContent(gridLayout);
	}

}

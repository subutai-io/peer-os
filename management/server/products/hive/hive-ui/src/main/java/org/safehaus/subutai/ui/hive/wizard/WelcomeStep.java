package org.safehaus.subutai.ui.hive.wizard;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.ui.hive.HiveUI;

public class WelcomeStep extends Panel {

	public WelcomeStep(final Wizard wizard) {

		setSizeFull();

		GridLayout grid = new GridLayout(10, 6);
		grid.setSpacing(true);
		grid.setMargin(true);
		grid.setSizeFull();

		Label welcomeMsg = new Label("<center><h2>Welcome to Hive Installation Wizard!</h2>");
		welcomeMsg.setContentMode(ContentMode.HTML);
		grid.addComponent(welcomeMsg, 3, 1, 6, 2);

		Label logoImg = new Label();
		logoImg.setIcon(new FileResource(FileUtil.getFile(HiveUI.MODULE_IMAGE, this)));
		logoImg.setContentMode(ContentMode.HTML);
		logoImg.setHeight(150, Unit.PIXELS);
		logoImg.setWidth(150, Unit.PIXELS);
		grid.addComponent(logoImg, 1, 3, 2, 5);

		Button next = new Button("Start");
		next.addStyleName("default");
		next.setWidth(100, Unit.PIXELS);
		grid.addComponent(next, 6, 4, 6, 4);
		grid.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.init();
				wizard.next();
			}
		});

		setContent(grid);
	}

}

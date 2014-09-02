package org.safehaus.subutai.plugin.elasticsearch.ui.wizard;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.FileUtil;
import org.safehaus.subutai.plugin.elasticsearch.ui.ElasticsearchUI;


public class StepStart extends VerticalLayout {

	public StepStart(final Wizard wizard) {
		setSizeFull();

		GridLayout gridLayout = new GridLayout(10, 6);
		gridLayout.setSizeFull();

		Label welcomeMsg = new Label(
				"<center><h2>Welcome to Elasticsearch Installation Wizard!</h2></center>"
		);
		welcomeMsg.addStyleName("h2");
		welcomeMsg.setContentMode(ContentMode.HTML);
		gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

		Label logoImg = new Label();
		logoImg.setIcon(new FileResource(FileUtil.getFile(ElasticsearchUI.MODULE_IMAGE, this)));
		logoImg.setContentMode(ContentMode.HTML);
		logoImg.setHeight(150, Unit.PIXELS);
		logoImg.setWidth(220, Unit.PIXELS);
		gridLayout.addComponent(logoImg, 1, 3, 2, 5);

		Button next = new Button("Start");
		next.addStyleName("default");
		next.setWidth(100, Unit.PIXELS);
		gridLayout.addComponent(next, 6, 4, 6, 4);
		gridLayout.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.getConfig().reset();
				wizard.next();
			}
		});

		addComponent(gridLayout);
	}

}

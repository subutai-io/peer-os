package org.safehaus.subutai.ui.sqoop.wizard;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.FileUtil;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

public class WelcomeStep extends Panel {

    public WelcomeStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(10, 6);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label welcomeMsg = new Label("<center><h2>Welcome to Sqoop Installation Wizard!</h2>");
        welcomeMsg.setContentMode(ContentMode.HTML);
        grid.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new FileResource(FileUtil.getFile(SqoopUI.MODULE_IMAGE, this)));
        logoImg.setContentMode(ContentMode.HTML);
        logoImg.setHeight(150, Unit.PIXELS);
        logoImg.setWidth(150, Unit.PIXELS);
        grid.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Start over-Hadoop installation");
        next.addStyleName("default");
        next.addClickListener(new ClickListerner(wizard));
        grid.addComponent(next, 6, 4, 6, 4);
        grid.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        Button next2 = new Button("Start with-Hadoop installation");
        next2.addStyleName("default");
        next2.addClickListener(new ClickListerner(wizard));
        grid.addComponent(next, 7, 4, 7, 4);
        grid.setComponentAlignment(next2, Alignment.BOTTOM_RIGHT);

        setContent(grid);
    }

    private class ClickListerner implements Button.ClickListener {

        final Wizard wizard;

        public ClickListerner(Wizard wizard) {
            this.wizard = wizard;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            wizard.init();
            wizard.next();
        }

    }

}

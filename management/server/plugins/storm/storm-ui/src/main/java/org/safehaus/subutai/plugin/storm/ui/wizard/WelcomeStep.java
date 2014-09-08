package org.safehaus.subutai.plugin.storm.ui.wizard;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.storm.ui.StormUI;

public class WelcomeStep extends Panel {

    public WelcomeStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(10, 6);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label welcomeMsg = new Label("<center><h2>Welcome to Storm Installation Wizard!</h2>");
        welcomeMsg.setContentMode(ContentMode.HTML);
        grid.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new FileResource(FileUtil.getFile(StormUI.MODULE_IMAGE, this)));
        logoImg.setContentMode(ContentMode.HTML);
        logoImg.setHeight(150, Unit.PIXELS);
        logoImg.setWidth(150, Unit.PIXELS);
        grid.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Start (embedded Zookeeper)");
        next.addStyleName("default");
        next.addClickListener(new NextClickHandler(wizard, false));
        grid.addComponent(next, 6, 4, 6, 4);
        grid.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        Button nextExt = new Button("Start (external Zookeeper)");
        nextExt.addStyleName("default");
        nextExt.addClickListener(new NextClickHandler(wizard, true));
        grid.addComponent(nextExt, 7, 4, 7, 4);
        grid.setComponentAlignment(nextExt, Alignment.BOTTOM_RIGHT);

        setContent(grid);
    }

    private class NextClickHandler implements Button.ClickListener {

        final Wizard wizard;
        final boolean withExtZookeeper;

        public NextClickHandler(Wizard wizard, boolean withExtZookeeper) {
            this.wizard = wizard;
            this.withExtZookeeper = withExtZookeeper;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            wizard.init(withExtZookeeper);
            wizard.next();
        }

    }
}

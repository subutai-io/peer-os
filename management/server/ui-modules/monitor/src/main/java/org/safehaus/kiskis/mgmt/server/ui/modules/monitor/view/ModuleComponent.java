package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.JavaScript;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;

import java.util.*;
import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent {

    private final Logger log = Logger.getLogger(ModuleComponent.class.getName());
    private Chart chart;

    public ModuleComponent() {
        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(1000, Sizeable.UNITS_PIXELS);
        layout.setHeight(1000, Sizeable.UNITS_PIXELS);

        Button button = new Button("Test");
        button.setWidth(120, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handleButton();
            }
        });

        layout.addComponent(button, "left: 30px; top: 50px;");

        AbsoluteLayout layout2 = new AbsoluteLayout();
        layout2.setWidth(800, Sizeable.UNITS_PIXELS);
        layout2.setHeight(300, Sizeable.UNITS_PIXELS);
        layout2.setDebugId("chart");

        layout.addComponent(layout2, "left: 200px; top: 10px;");

        return layout;
    }

    private void handleButton() {

        if (chart == null) {
            chart = new Chart(getWindow());
        }

        Handler handler = HandlerFactory.getHandler(null);
        chart.load(handler);
    }
}
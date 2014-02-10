package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.Date;
import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final Logger log = Logger.getLogger(ModuleComponent.class.getName());
    private final String moduleName;

    private static boolean loaded;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;

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
                loadScripts();
                //getWindow().executeJavaScript("console.log( $('#subdiv') );");
                getWindow().executeJavaScript("$('#subdiv').html('" + new Date() + "');");
            }
        });

        layout.addComponent(button, "left: 30px; top: 50px;");

        AbsoluteLayout layout2 = new AbsoluteLayout();
        layout2.setWidth(300, Sizeable.UNITS_PIXELS);
        layout2.setHeight(300, Sizeable.UNITS_PIXELS);
        layout2.setDebugId("subdiv");

        layout.addComponent(layout2, "left: 200px; top: 10px;");

        return layout;
    }

    private void loadScripts() {

        if (loaded) {
            return;
        }

        final String jquery = FileUtil.getContent("js/jquery.min.js");
        getWindow().executeJavaScript(jquery);

        loaded = true;
    }

    @Override
    public void onCommand(Response response) {
        //CommandExecutor.INSTANCE.onResponse(response);
    }

    @Override
    public String getName() {
        return moduleName;
    }

}
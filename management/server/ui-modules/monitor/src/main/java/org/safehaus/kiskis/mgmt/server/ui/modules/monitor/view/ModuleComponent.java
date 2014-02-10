package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final Logger log = Logger.getLogger(ModuleComponent.class.getName());
    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;

        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(400, Sizeable.UNITS_PIXELS);
        layout.setHeight(400, Sizeable.UNITS_PIXELS);

        Button button = new Button("Test");
        button.setWidth(120, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                String s = FileUtil.getContent("js/snippet.js");
                String code = "function hello(a) { console.log(a); }; hello('hello');";
                s = s.replace("${code}", code);

                getWindow().executeJavaScript(s);
            }
        });

        layout.addComponent(button, "left: 30px; top: 50px;");

        AbsoluteLayout layout2 = new AbsoluteLayout();
        layout2.setWidth(100, Sizeable.UNITS_PIXELS);
        layout2.setHeight(200, Sizeable.UNITS_PIXELS);
        layout2.setDebugId("subdiv");

        layout.addComponent(layout2, "left: 100px; top: 10px;");

        return layout;
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
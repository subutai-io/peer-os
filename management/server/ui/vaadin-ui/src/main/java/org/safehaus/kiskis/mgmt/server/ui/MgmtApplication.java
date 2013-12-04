package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.Iterator;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener {

    private ModuleServiceListener app;
    private ModuleService moduleService;
    private AgentManagerInterface agentManagerService;
    private Window window;

    public MgmtApplication(String title, ModuleService moduleService, AgentManagerInterface agentManagerService) {
        this.moduleService = moduleService;
        this.agentManagerService = agentManagerService;
        this.title = title;
    }
    private String title;
    private TabSheet tabs;

    @Override
    public void init() {
        app = this;
        window = new Window(title);
        // Create the application data instance
        AppData sessionData = new AppData(this);

        // Register it as a listener in the application context
        getContext().addTransactionListener(sessionData);

        setMainWindow(window);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
        layout.addComponent(horizontalSplit);

        layout.setExpandRatio(horizontalSplit, 1);
        horizontalSplit.setSplitPosition(200, SplitPanel.UNITS_PIXELS);

        horizontalSplit.setFirstComponent(new MgmtAgentManager(agentManagerService));

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.setImmediate(true);

        for (Module module : moduleService.getModules()) {
            tabs.addTab(module.createComponent(), module.getName(), null);
        }
        horizontalSplit.setSecondComponent(tabs);

        getMainWindow().setContent(layout);
        setTheme("runo");

        moduleService.addListener(this);
        getMainWindow().addListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                try {
                    if (moduleService != null) {
                        moduleService.removeListener(app);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        setInterval(5000);
    }

    private void setInterval(int ms) {
        try {
            getMainWindow().executeJavaScript(
                    "try{if(intervalHandle){window.clearInterval(intervalHandle);}}catch(e){}; "
                    + "intervalHandle = window.setInterval(function(){try{javascript:vaadin.forceSync()}catch(e){}},"
                    + ms + ");");
        } catch (Exception e) {
        }
    }

    @Override
    public void close() {
        System.out.println("Kiskis Management Vaadin UI: Application closing, removing module service listener");
        super.close();
    }

    public void moduleRegistered(ModuleService source, Module module) {
        System.out.println("Kiskis Management Vaadin UI: Module registered, adding tab");
        tabs.addTab(module.createComponent(), module.getName(), null);
        setInterval(5000);
    }

    public void moduleUnregistered(ModuleService source, Module module) {
        System.out.println("Kiskis Management Vaadin UI: Module unregistered, removing tab");
        Iterator<Component> it = tabs.getComponentIterator();
        setInterval(5000);
        while (it.hasNext()) {
            Component c = it.next();
            if (tabs.getTab(c).getCaption().equals(module.getName())) {
                tabs.removeComponent(c);
                return;
            }
        }

    }
}

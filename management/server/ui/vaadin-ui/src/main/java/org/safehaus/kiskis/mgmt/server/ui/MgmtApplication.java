package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.Iterator;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener {

    private ModuleService moduleService;
    private AgentManagerInterface agentManagerService;

    public MgmtApplication(String title, ModuleService moduleService, AgentManagerInterface agentManagerService) {
        this.moduleService = moduleService;
        this.agentManagerService = agentManagerService;
        this.title = title;
    }

    private String title;
    private TabSheet tabs;
    private MgmtAgentManager agents;

    @Override
    public void init() {
        // Create the application data instance
        AppData sessionData = new AppData(this);

        setMainWindow(new Window(title));

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        layout.addComponent(horizontalSplit);

        layout.setExpandRatio(horizontalSplit, 1);
        horizontalSplit.setSplitPosition(200, SplitPanel.UNITS_PIXELS);

        agents = new MgmtAgentManager(this.agentManagerService);
        horizontalSplit.setFirstComponent(agents);

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
        getMainWindow().executeJavaScript("setInterval(function() { javascript:vaadin.forceSync(); }, 5000);");
    }

    @Override
    public void close() {
        System.out.println("Kiskis Management Vaadin UI: Application closing, removing module service listener");
        moduleService.removeListener(this);
        super.close();
    }

    public void moduleRegistered(ModuleService source, Module module) {
        System.out.println("Kiskis Management Vaadin UI: Module registered, adding tab");
        tabs.addTab(module.createComponent(), module.getName(), null);
    }

    public void moduleUnregistered(ModuleService source, Module module) {
        System.out.println("Kiskis Management Vaadin UI: Module unregistered, removing tab");
        Iterator<Component> it = tabs.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (tabs.getTab(c).getCaption().equals(module.getName())) {
                tabs.removeComponent(c);
                return;
            }
        }
    }
}

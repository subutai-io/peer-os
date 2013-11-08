package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener {

    private ModuleService moduleService;

    public MgmtApplication(String title, ModuleService moduleService) {
        this.moduleService = moduleService;
        this.title = title;
    }

    private String title;
    private TabSheet tabs;

    @Override
    public void init() {
        tabs = new TabSheet();
        tabs.setSizeFull();

        for (Module module : moduleService.getModules()) {
            tabs.addTab(module.createComponent(), module.getName(), null);
        }

        setMainWindow(new Window(title, tabs));
        setTheme("runo");

        moduleService.addListener(this);
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

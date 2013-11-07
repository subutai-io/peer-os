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

//    @WebServlet(urlPatterns = "/*")
    public static class Servlet extends AbstractApplicationServlet {

        @Resource(mappedName = "vaadin-moduleService")
        ModuleService moduleService;

        @Override
        protected Class<? extends Application> getApplicationClass() {
            return MgmtApplication.class;
        }

        @Override
        protected Application getNewApplication(HttpServletRequest request)
                throws ServletException {
            return new MgmtApplication(moduleService);
        }

    }

    private ModuleService moduleService;

    public MgmtApplication(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    private TabSheet tabs;

    @Override
    public void init() {
        tabs = new TabSheet();
        tabs.setSizeFull();

        for (Module module : moduleService.getModules()) {
            tabs.addTab(module.createComponent(), module.getName(), null);
        }

        setMainWindow(new Window("Module Demo Application", tabs));

        System.out.println("ModuleDemoApp: Application initializing, adding module service listener");
        moduleService.addListener(this);
    }

    @Override
    public void close() {
        System.out.println("ModuleDemoApp: Application closing, removing module service listener");
        moduleService.removeListener(this);
        super.close();
    }

    public void moduleRegistered(ModuleService source, Module module) {
        System.out.println("ModuleDemoApp: Module registered, adding tab");
        tabs.addTab(module.createComponent(), module.getName(), null);
    }

    public void moduleUnregistered(ModuleService source, Module module) {
        System.out.println("ModuleDemoApp: Module unregistered, removing tab");
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

package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
//import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener, HttpServletRequestListener {

    private static final Logger LOG = Logger.getLogger(MgmtApplication.class.getName());
    private ModuleServiceListener app;
    private final ModuleService moduleService;
    private final AgentManagerInterface agentManagerService;
    private Window window;
    private static final ThreadLocal<MgmtApplication> threadLocal = new ThreadLocal<MgmtApplication>();
    private Set<Agent> selectedAgents = new HashSet<Agent>();

    public MgmtApplication(String title, ModuleService moduleService, AgentManagerInterface agentManagerService) {
        this.moduleService = moduleService;
        this.agentManagerService = agentManagerService;
        this.title = title;
    }
    private final String title;
    private TabSheet tabs;

    @Override
    public void init() {
        setInstance(this);
        try {
            setTheme(Runo.themeName());

            app = this;
            window = new Window(title);
            setMainWindow(window);

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
            horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
            layout.addComponent(horizontalSplit);

            layout.setExpandRatio(horizontalSplit, 1);
            horizontalSplit.setSplitPosition(200, Sizeable.UNITS_PIXELS);

            Panel panel = new Panel();
            panel.addComponent(new MgmtAgentManager(agentManagerService));
            panel.setSizeFull();
            horizontalSplit.setFirstComponent(panel);

            tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.setImmediate(true);
            for (Module module : moduleService.getModules()) {
                tabs.addTab(module.createComponent(), module.getName(), null);
            }
            horizontalSplit.setSecondComponent(tabs);

            getMainWindow().setContent(layout);

            moduleService.addListener(this);
            getMainWindow().addListener(new Window.CloseListener() {
                @Override
                public void windowClose(Window.CloseEvent e) {
                    try {
                        if (moduleService != null) {
                            LOG.log(Level.INFO, "Removing app as module listener");
                            moduleService.removeListener(app);
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Error in windowClose", ex);
                    }
                }
            });
            //
            final ProgressIndicator indicator
                    = new ProgressIndicator(new Float(0.0));
            indicator.setPollingInterval(3000);
            indicator.setWidth("1px");
            indicator.setHeight("1px");
            getMainWindow().addComponent(indicator);
            //            

        } catch (Exception ex) {
        } finally {
        }
    }

    @Override
    public void close() {
        super.close();
        LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Application closing, removing module service listener");
    }

    @Override
    public void moduleRegistered(ModuleService source, Module module) {
        LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module registered, adding tab");
        tabs.addTab(module.createComponent(), module.getName(), null);
    }

    @Override
    public void moduleUnregistered(ModuleService source, Module module) {
        LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module unregistered, removing tab");
        Iterator<Component> it = tabs.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (tabs.getTab(c).getCaption().equals(module.getName())) {
                tabs.removeComponent(c);
                return;
            }
        }
    }

    public static MgmtApplication getInstance() {
        return threadLocal.get();
    }

    public static void setInstance(MgmtApplication application) {
        threadLocal.set(application);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        MgmtApplication.setInstance(this);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        threadLocal.remove();
    }

    public static Set<Agent> getSelectedAgents() {
        return getInstance().selectedAgents;
    }

    public static void setSelectedAgents(Set<Agent> agents) {
        getInstance().selectedAgents = agents;
    }

}

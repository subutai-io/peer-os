package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import java.util.ArrayList;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener/*, HttpServletRequestListener*/ {

    private static final Logger LOG = Logger.getLogger(MgmtApplication.class.getName());
//    private static final ThreadLocal<MgmtApplication> threadLocal = new ThreadLocal<MgmtApplication>();
    private ModuleServiceListener app;
    private final ModuleService moduleService;
    private final AgentManagerInterface agentManagerService;
    private Window window;
//    private List<Agent> selectedAgents = new ArrayList<Agent>();
    private static final Map<String, TimeoutWrapper> selectedAgents = new ConcurrentHashMap<String, TimeoutWrapper>();
    private static int sessionTimeoutSec = 300;

    static class TimeoutWrapper {

        private final List<Agent> agents;
        private final long ts;

        public TimeoutWrapper(List<Agent> agents) {
            this.agents = agents;
            ts = System.currentTimeMillis();
        }

        public List<Agent> getAgents() {
            return agents;
        }

        public long getTs() {
            return ts;
        }

    }

    public MgmtApplication(String title, ModuleService moduleService, AgentManagerInterface agentManagerService) {
        this.moduleService = moduleService;
        this.agentManagerService = agentManagerService;
        this.title = title;
    }
    private final String title;
    private TabSheet tabs;

    @Override
    public void init() {
//        setInstance(this);
        try {
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
            setTheme("runo");

            moduleService.addListener(this);
            getMainWindow().addListener(new Window.CloseListener() {
                @Override
                public void windowClose(Window.CloseEvent e) {
                    try {
                        if (moduleService != null) {
                            LOG.log(Level.INFO, "Removing app as module listener");
                            moduleService.removeListener(app);
                            //clean session data
                            selectedAgents.remove(Thread.currentThread().getName());
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

            ApplicationContext context = getContext();
            if (context instanceof WebApplicationContext) {
                sessionTimeoutSec = ((WebApplicationContext) context).getHttpSession().getMaxInactiveInterval();
            }
        } catch (Exception ex) {
        } finally {
        }
    }

    @Override
    public void close() {
        super.close();
        LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Application closing, removing module service listener");
        //clean all expired session data
        for (Map.Entry<String, TimeoutWrapper> agent : selectedAgents.entrySet()) {
            if (System.currentTimeMillis() - agent.getValue().getTs() >= sessionTimeoutSec * 1000) {
                selectedAgents.remove(agent.getKey());
            }
        }
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

//    public static MgmtApplication getInstance() {
//        return threadLocal.get();
//    }
//
//    public static void setInstance(MgmtApplication application) {
//        threadLocal.set(application);
//    }
//    
//    @Override
//    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
//        MgmtApplication.setInstance(this);
//    }
//
//    @Override
//    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
//        threadLocal.remove();
//    }
    public static List<Agent> getSelectedAgents() {
        TimeoutWrapper tw = selectedAgents.get(Thread.currentThread().getName());
        if (tw != null) {
            return tw.getAgents();
        }
        return null;
    }

    public static void setSelectedAgents(List<Agent> agents) {
        selectedAgents.put(Thread.currentThread().getName(), new TimeoutWrapper(agents));
    }

    public static List<List<Agent>> getAllSessionsSelectedAgents() {
        List<List<Agent>> allSessionsAgents = new ArrayList<List<Agent>>();
        for (TimeoutWrapper tw : selectedAgents.values()) {
            allSessionsAgents.add(tw.getAgents());
        }
        return allSessionsAgents;
    }

}

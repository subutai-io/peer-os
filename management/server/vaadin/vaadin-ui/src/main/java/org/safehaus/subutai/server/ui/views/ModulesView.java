/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package org.safehaus.subutai.server.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.safehaus.subutai.server.ui.MainUI;

import java.util.logging.Logger;

public class ModulesView extends VerticalLayout implements View {

    private static final Logger LOG = Logger.getLogger(MainUI.class.getName());
    private TabSheet tabs;
    /*private CommandManager commandManager;
    private ModuleNotifier moduleNotifier;*/

    public ModulesView() {
        setSizeFull();
        addStyleName("dashboard-view");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setSpacing(true);
        top.addStyleName("toolbar");
        addComponent(top);
        
        final Label title = new Label("My Dashboard");
        title.setSizeUndefined();
        title.addStyleName("h1");
        top.addComponent(title);
        top.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
        top.setExpandRatio(title, 1);

        HorizontalLayout row = new HorizontalLayout();
        row.setSizeFull();
        row.setMargin(new MarginInfo(true, true, false, true));
        row.setSpacing(true);
        addComponent(row);
        setExpandRatio(row, 1.5f);
        
        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName("borderless");
        addComponent(tabs);

        /*this.commandManager = ServiceLocator.getService(CommandManager.class);
        this.moduleNotifier = ServiceLocator.getService(ModuleNotifier.class);

        for (Module module : moduleNotifier.getModules()) {
            Component component = module.createComponent();
            tabs.addTab(component, module.getName(), null);
            if (component instanceof CommandListener) {
                commandManager.addListener((CommandListener) component);
            }
        }
        moduleNotifier.addListener(this);*/
    }

    @Override
    public void enter(ViewChangeEvent event) {
    }

    /*@Override
    public void moduleRegistered(Module module) {
        try {
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module registered, adding tab");
            Component component = module.createComponent();
            tabs.addTab(component, module.getName(), null);
            if (component instanceof CommandListener) {
                commandManager.addListener((CommandListener) component);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error registering module{0}", e);
        }
    }

    @Override
    public void moduleUnregistered(Module module) {
        try {
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module unregistered, removing tab");
            Iterator<Component> it = tabs.getComponentIterator();
            while (it.hasNext()) {
                Component component = it.next();
                if (tabs.getTab(component).getCaption().equals(module.getName())) {
                    tabs.removeComponent(component);
                    if (component instanceof CommandListener) {
                        commandManager.removeListener((CommandListener) component);
                    }
                    return;
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error unregistering module{0}", e);
        }
    }*/
}

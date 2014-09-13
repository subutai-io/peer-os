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

import com.vaadin.event.LayoutEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.subutai.server.ui.MainUI;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.server.ui.api.PortalModuleListener;
import org.safehaus.subutai.server.ui.api.PortalModuleService;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModulesView extends VerticalLayout implements View, PortalModuleListener {

    private static final Logger LOG = Logger.getLogger(MainUI.class.getName());
    private TabSheet editors;
    private CssLayout modulesLayout;
    private HashMap<String, PortalModule> modules = new HashMap<>();
    private HashMap<String, PortalModuleView> portalModuleViews = new HashMap<>();


    @Override
    public void enter(ViewChangeEvent event) {
        setSizeFull();
        addStyleName("reports");

        addComponent(buildDraftsView());
        getPortalModuleService().addListener(this);
    }

    private Component buildDraftsView() {
        editors = new TabSheet();
        editors.setSizeFull();
        editors.addStyleName("borderless");
        editors.addStyleName("editors");

        editors.setCloseHandler(new TabSheet.CloseHandler() {
            @Override
            public void onTabClose(TabSheet components, Component component) {
                editors.removeComponent(component);
                modules.remove(component.getId());
            }
        });

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setCaption("Modules");
        titleAndDrafts.setSpacing(true);
        titleAndDrafts.addStyleName("drafts");
        editors.addComponent(titleAndDrafts);

        Label draftsTitle = new Label("Modules");
        draftsTitle.addStyleName("h1");
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent(draftsTitle);
        titleAndDrafts.setComponentAlignment(draftsTitle, Alignment.TOP_CENTER);

        modulesLayout = new CssLayout();
        modulesLayout.setSizeUndefined();
        modulesLayout.addStyleName("catalog");
        titleAndDrafts.addComponent(modulesLayout);

        Notification.show("Adding new modules",
                Notification.Type.ERROR_MESSAGE);
        for (PortalModule module : getPortalModuleService().getModules()) {
            addModule(module);
        }

        return editors;
    }

    public static PortalModuleService getPortalModuleService() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(PortalModuleService.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(PortalModuleService.class.getName());
            if (serviceReference != null) {
                return PortalModuleService.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    private void addModule(final PortalModule module) {

        if (module == null) {
            LOG.log(Level.SEVERE, "Module to add was null value.");
        }
        LOG.log(Level.WARNING, "Adding module: " + module.getId());

        PortalModuleView portalModuleView = new PortalModuleView(module, new PortalModelViewListener() {
            @Override
            public void onItemClick(PortalModuleView module) {
                if (!portalModuleViews.containsKey(module.getId())) {
                    autoCreate(module.getPortalModule());
                    portalModuleViews.put(module.getId(), module);
                }
            }
        });

        modulesLayout.addComponent(portalModuleView);


        CssLayout moduleLayout = new CssLayout();
        moduleLayout.setId(module.getId());
        moduleLayout.setWidth(150, Unit.PIXELS);
        moduleLayout.setHeight(200, Unit.PIXELS);
        moduleLayout.addStyleName("create");

        moduleLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                if (!modules.containsKey(module.getId())) {
                    autoCreate(module);
                    modules.put(module.getId(), module);
                }
            }
        });

        Image image = new Image("", new FileResource(module.getImage()));
        image.setWidth(90, Unit.PERCENTAGE);
        image.setDescription(module.getName());
        moduleLayout.addComponent(image);

        modulesLayout.addComponent(moduleLayout);
    }

    public void autoCreate(PortalModule module) {
        Component component = module.createComponent();
        component.setId(module.getId());
        TabSheet.Tab tab = editors.addTab(component);
        tab.setCaption(module.getName());
        tab.setClosable(true);
        editors.setSelectedTab(tab);
//        Notification.show("Creating module", module.getName(), Notification.Type.WARNING_MESSAGE);
    }

    @Override
    public void moduleRegistered(PortalModule module) {

//        MySub mySub = new MySub();
//        modulesLayout.addComponent(mySub);
//        this.getUI().addWindow(mySub);

        Notification.show("Registering new module", Notification.Type.ERROR_MESSAGE);
        LOG.warning(module.toString());
        this.addModule(module);
//        addModule(module);
//        String caption = "Registering new module: ";
//        try {
//            Notification.show(caption, module.getName() + " " + module.getId(), Notification.Type.HUMANIZED_MESSAGE);
//        } catch (NullPointerException ex) {
//
//            Notification.show("Registering new module is null!!!");
//        }
    }

    @Override
    public void moduleUnregistered(PortalModule module) {
//        Notification.show("Unregistered module");
        addModule(null);
    }

    public interface PortalModelViewListener {
        public void onItemClick(PortalModuleView module);
    }
}


package org.safehaus.kiskis.mgmt.server.ui.modules;


import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;

public class Module1 implements Module {

    public static class ModuleComponent extends CustomComponent {

        public ModuleComponent() {
            setCompositionRoot(new Label("Hello, this is Module 1"));
        }

    }

    public String getName() {
        return "Module 1";
    }

    public Component createComponent() {
        return new ModuleComponent();
    }

    public void setModuleService(ModuleService service) {
        System.out.println("Module1: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        System.out.println("Module1: unregistering with ModuleService");
        service.unregisterModule(this);
    }
}
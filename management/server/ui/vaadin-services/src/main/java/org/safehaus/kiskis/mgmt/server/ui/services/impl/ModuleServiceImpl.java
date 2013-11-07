package org.safehaus.kiskis.mgmt.server.ui.services.impl;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;

import java.util.*;

public class ModuleServiceImpl implements ModuleService {

    private ArrayList<Module> modules = new ArrayList<Module>();

    private ArrayList<ModuleServiceListener> listeners = new ArrayList<ModuleServiceListener>();

    @SuppressWarnings("unchecked")
    public synchronized void registerModule(Module module) {
        System.out.println("ModuleServiceImpl: Registering module " + module);
        modules.add(module);
        for (ModuleServiceListener listener : (ArrayList<ModuleServiceListener>) listeners.clone()) {
            listener.moduleRegistered(this, module);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void unregisterModule(Module module) {
        System.out.println("ModuleServiceImpl: Unregistering module " + module);
        modules.remove(module);
        for (ModuleServiceListener listener : (ArrayList<ModuleServiceListener>) listeners.clone()) {
            listener.moduleUnregistered(this, module);
        }
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public synchronized void addListener(ModuleServiceListener listener) {
        System.out.println("ModuleServiceImpl: Adding listener " + listener);
        listeners.add(listener);
    }

    public synchronized void removeListener(ModuleServiceListener listener) {
        System.out.println("ModuleServiceImpl: Removing listener " + listener);
        listeners.remove(listener);
    }


}
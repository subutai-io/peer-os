package org.safehaus.kiskis.mgmt.server.ui.services.impl;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleServiceImpl implements ModuleService {

    private ArrayList<Module> modules = new ArrayList<Module>();

    private ArrayList<ModuleServiceListener> listeners = new ArrayList<ModuleServiceListener>();

    @SuppressWarnings("unchecked")
    public synchronized void registerModule(Module module) {
        System.out.println("ModuleServiceImpl: Registering module " + module);
        try {
            modules.add(module);
            for (ModuleServiceListener listener : (ArrayList<ModuleServiceListener>) listeners.clone()) {
                listener.moduleRegistered(this, module);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void unregisterModule(Module module) {
        System.out.println("ModuleServiceImpl: Unregistering module " + module);
        try {
            modules.remove(module);
            for (ModuleServiceListener listener : (ArrayList<ModuleServiceListener>) listeners.clone()) {
                listener.moduleUnregistered(this, module);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public synchronized void addListener(ModuleServiceListener listener) {
        System.out.println("ModuleServiceImpl: Adding listener " + listener);
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void removeListener(ModuleServiceListener listener) {
        if(listener !=null){
            System.out.println("ModuleServiceImpl: Removing listener " + listener);
            try {
                listeners.remove(listener);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
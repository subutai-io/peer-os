package org.safehaus.kiskis.mgmt.server.ui.services.impl;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuleServiceImpl implements ModuleService {

    private static final Logger LOG = Logger.getLogger(ModuleServiceImpl.class.getName());

    private final Queue<Module> modules = new ConcurrentLinkedQueue<Module>();

    private final Queue<ModuleServiceListener> listeners = new ConcurrentLinkedQueue<ModuleServiceListener>();

    @SuppressWarnings("unchecked")
    @Override
    public void registerModule(Module module) {
        try {
            LOG.log(Level.INFO, "ModuleServiceImpl: Registering module {0}", module);
            modules.add(module);
            for (ModuleServiceListener listener : listeners) {
                listener.moduleRegistered(this, module);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in registerModule", ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unregisterModule(Module module) {
        try {
            LOG.log(Level.INFO, "ModuleServiceImpl: Unregistering module {0}", module);
            modules.remove(module);
            for (ModuleServiceListener listener : listeners) {
                listener.moduleUnregistered(this, module);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in unregisterModule", ex);
        }
    }

    @Override
    public Queue<Module> getModules() {
        return modules;
    }

    @Override
    public void addListener(ModuleServiceListener listener) {
        if (listener != null) {
            try {
                LOG.log(Level.INFO, "ModuleServiceImpl: Adding listener {0}", listener);
                listeners.add(listener);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in addListener", ex);
            }
        }
    }

    @Override
    public void removeListener(ModuleServiceListener listener) {
        if (listener != null) {
            try {
                LOG.log(Level.INFO, "ModuleServiceImpl: Removing listener {0}", listener);
                listeners.remove(listener);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in removeListener", ex);
            }
        }
    }
}

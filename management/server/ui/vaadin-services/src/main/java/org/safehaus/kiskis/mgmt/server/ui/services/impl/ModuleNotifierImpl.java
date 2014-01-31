/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.services.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 *
 * @author dilshat
 */
public class ModuleNotifierImpl implements ModuleNotifier {

    private final Queue<Module> modules = new ConcurrentLinkedQueue<Module>();
    private final Queue<ModuleServiceListener> moduleListeners = new ConcurrentLinkedQueue<ModuleServiceListener>();
    private final CommandManager commandManager;

    public ModuleNotifierImpl() {
        commandManager = ServiceLocator.getService(CommandManager.class);
    }

    @Override
    public Queue<Module> getModules() {
        return modules;
    }

    @Override
    public Queue<ModuleServiceListener> getListeners() {
        return moduleListeners;
    }

    public void setModule(Module module) {
        modules.add(module);
        if (module instanceof CommandListener) {
            commandManager.addListener((CommandListener) module);
        }
        for (ModuleServiceListener moduleServiceListener : moduleListeners) {
            moduleServiceListener.moduleRegistered(module);
        }
    }

    public void unsetModule(Module module) {
        modules.remove(module);
        if (module instanceof CommandListener) {
            commandManager.removeListener((CommandListener) module);
        }
        for (ModuleServiceListener moduleServiceListener : moduleListeners) {
            moduleServiceListener.moduleUnregistered(module);
        }
    }

    @Override
    public void addListener(ModuleServiceListener listener) {
        moduleListeners.add(listener);
    }

    @Override
    public void removeListener(ModuleServiceListener listener) {
        moduleListeners.remove(listener);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.services.impl;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author dilshat
 */
public class ModuleNotifierImpl implements ModuleNotifier {

    private final Queue<Module> modules = new PriorityBlockingQueue<Module>(32, new Comparator<Module>() {
        @Override
        public int compare(Module o1, Module o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    });
    private final Queue<ModuleServiceListener> moduleListeners = new ConcurrentLinkedQueue<ModuleServiceListener>();

    @Override
    public Queue<Module> getModules() {
        return modules;
    }

    @Override
    public Queue<ModuleServiceListener> getListeners() {
        return moduleListeners;
    }

    public void setModule(Module module) {
        try {
            modules.add(module);
            for (ModuleServiceListener moduleServiceListener : moduleListeners) {
                try {
                    moduleServiceListener.moduleRegistered(module);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    public void unsetModule(Module module) {
        try {
            modules.remove(module);
            for (ModuleServiceListener moduleServiceListener : moduleListeners) {
                try {
                    moduleServiceListener.moduleUnregistered(module);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
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

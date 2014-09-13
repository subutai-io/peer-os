package org.safehaus.subutai.server.ui.impl;

import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.server.ui.api.PortalModuleListener;
import org.safehaus.subutai.server.ui.api.PortalModuleService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PortalModuleServiceImpl implements PortalModuleService {

    private static final Logger LOG = Logger.getLogger(PortalModuleServiceImpl.class.getName());

	private List<PortalModule> modules = Collections.synchronizedList(new ArrayList<PortalModule>());

	private List<PortalModuleListener> listeners = Collections.synchronizedList(new ArrayList<PortalModuleListener>());

	public void registerModule(PortalModule module) {
        LOG.info("ModuleServiceImpl: Registering module: " + module.getName() + " " + module.getId());
		System.out.println("ModuleServiceImpl: Registering module " + module.getId());
		modules.add(module);
		for (PortalModuleListener listener : listeners) {
			listener.moduleRegistered(module);
		}
	}

	public void unregisterModule(PortalModule module) {

        LOG.log(Level.WARNING, "ModuleServiceImpl: Unregister module.");
		if (module != null) {
            LOG.info("ModuleServiceImpl: Unregister module: " + module.getName() + " " + module.getId());
			System.out.println("ModuleServiceImpl: Unregister module " + module.getId());
			modules.remove(module);
			for (PortalModuleListener listener : listeners) {
				listener.moduleUnregistered(module);
			}
		} else {
            LOG.log(Level.WARNING, "ModuleServiceImpl: Unregister module is null");
        }
	}

	@Override
	public PortalModule getModule(String pModuleId) {
		for (PortalModule module : modules) {
			if (pModuleId.equals(module.getId())) {
				return module;
			}
		}
		throw new IllegalArgumentException("Cannot find any module with the id given");
	}

	public List<PortalModule> getModules() {
		return Collections.unmodifiableList(modules);
	}

	public synchronized void addListener(PortalModuleListener listener) {
		System.out.println("ModuleServiceImpl: Adding listener " + listener);
		listeners.add(listener);
	}

	public synchronized void removeListener(PortalModuleListener listener) {
		if (listener != null) {
			System.out.println("ModuleServiceImpl: Removing listener " + listener);
			listeners.remove(listener);
		}
	}

}
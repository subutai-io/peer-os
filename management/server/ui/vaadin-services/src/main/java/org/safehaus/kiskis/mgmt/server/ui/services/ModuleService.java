package org.safehaus.kiskis.mgmt.server.ui.services;

import java.util.Queue;

public interface ModuleService {

    public void registerModule(Module module);

    public void unregisterModule(Module module);

    public Queue<Module> getModules();

    public void addListener(ModuleServiceListener listener);

    public void removeListener(ModuleServiceListener listener);

}

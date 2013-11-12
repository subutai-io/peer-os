package org.safehaus.kiskis.mgmt.server.ui.services;

import java.util.List;

public interface ModuleService {

    public void registerModule(Module module);

    public void unregisterModule(Module module);

    public List<Module> getModules();

    public void addListener(ModuleServiceListener listener);

    public void removeListener(ModuleServiceListener listener);

}

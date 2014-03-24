package org.safehaus.kiskis.mgmt.server.ui.services;

public interface ModuleServiceListener {

    public void moduleRegistered(Module module);

    public void moduleUnregistered(Module module);

}

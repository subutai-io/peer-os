package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

public class HandlerFactory {

    public static Handler getHandler(DataType dataType) {
        return new MemoryHandler();
    }

}

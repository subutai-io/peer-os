package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

public class HandlerFactory {

    private static final Handler memoryHandler = new MemoryHandler();
    private static final Handler cpuHandler = new CpuHandler();
    private static final Handler diskHandler = new DiskHandler();
    private static final Handler networkHandler = new NetworkHandler();

    public static Handler getHandler(MetricType metricType) {

        Handler handler = memoryHandler;

        switch (metricType) {
        case CPU:
            handler = cpuHandler;
            break;
        case DISK:
            handler = diskHandler;
            break;
        case NETWORK:
            handler = networkHandler;
            break;
        }

        return handler;
    }

}

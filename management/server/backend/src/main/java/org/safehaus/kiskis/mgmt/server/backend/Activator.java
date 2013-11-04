package org.safehaus.kiskis.mgmt.server.backend;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/30/13 Time: 3:02 PM To
 * change this template use File | Settings | File Templates.
 */
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

/**
 *
 * @author bahadyr
 */
public class Activator implements BundleActivator {

    private static BundleContext context;
    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    public void start(BundleContext context) throws Exception {
        try {
            Activator.context = context;
            System.out.println("UI Activator started");
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void stop(BundleContext bc) throws Exception {
        try {
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static RegisteredHostInterface getServerBroker() {
        ServiceReference refServerBroker
                = Activator.context.getServiceReference(RegisteredHostInterface.class.getName());
        return ((RegisteredHostInterface) Activator.context.getService(refServerBroker));
    }

//    public static CommandSendInterface getCommandSender() {
//        ServiceReference refCommandSender =
//                Activator.context.getServiceReference(CommandSendInterface.class.getName());
//        return ((CommandSendInterface) Activator.context.getService(refCommandSender));
//    }
}

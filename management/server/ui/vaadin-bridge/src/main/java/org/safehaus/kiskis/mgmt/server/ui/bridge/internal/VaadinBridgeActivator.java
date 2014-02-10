package org.safehaus.kiskis.mgmt.server.ui.bridge.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class VaadinBridgeActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        new HttpServiceTracker(context).open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}

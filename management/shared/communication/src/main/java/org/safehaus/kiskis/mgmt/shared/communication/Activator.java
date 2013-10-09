package org.safehaus.kiskis.mgmt.shared.communication;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * BundleActivator for mgmt-server bundle
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println(context.getBundle().getSymbolicName() + " is started...\n");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println(context.getBundle().getSymbolicName() + " is stopped...\n");
    }

}

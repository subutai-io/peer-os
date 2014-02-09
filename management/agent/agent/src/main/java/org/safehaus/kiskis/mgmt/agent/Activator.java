package org.safehaus.kiskis.mgmt.agent;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * BundleActivator for the mgmt-agent bundle
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

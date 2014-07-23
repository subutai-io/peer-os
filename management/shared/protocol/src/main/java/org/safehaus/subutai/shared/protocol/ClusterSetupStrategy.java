package org.safehaus.subutai.shared.protocol;


/**
 * This interface must be implemented by all product modules.
 *
 * This is a strategy used to setup a product cluster
 */
public interface ClusterSetupStrategy {

    public ConfigBase setup() throws ClusterSetupException;
}

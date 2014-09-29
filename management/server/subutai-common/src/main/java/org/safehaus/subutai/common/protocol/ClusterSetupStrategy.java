package org.safehaus.subutai.common.protocol;


import org.safehaus.subutai.common.exception.ClusterSetupException;


/**
 * This interface must be implemented by all product modules. <p/> This is a strategy used to setup a product cluster
 */
public interface ClusterSetupStrategy
{
    public ConfigBase setup() throws ClusterSetupException;
}

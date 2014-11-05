package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.ConfigBase;


public interface ClusterConfigurationInterface{

    /**
     * Configures cluster with the given configuration
     * @param config cluster configuration object
     * @throws ClusterConfigurationException
     */
    public void configureCluster( ConfigBase config ) throws ClusterConfigurationException;
}

package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface ClusterConfigurationInterface<T extends ConfigBase>
{

    /**
     * Configures cluster with the given configuration
     *
     * @param config cluster configuration object
     * @param environment TODO
     */
    public void configureCluster( T config, Environment environment ) throws ClusterConfigurationException;
}

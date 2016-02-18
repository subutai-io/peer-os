package io.subutai.core.plugincommon.api;


import io.subutai.common.environment.Environment;


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

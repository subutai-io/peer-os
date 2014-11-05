package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.ConfigBase;


public interface ClusterConfigurationInterface{
    public void configureCluster( ConfigBase config ) throws ClusterConfigurationException;
}

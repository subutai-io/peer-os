package org.safehaus.subutai.configuration.manager.impl.utils;


import org.apache.cassandra.exceptions.ConfigurationException;


/**
 * Created by bahadyr on 7/9/14.
 */
public interface ConfigurationLoader {

    public Object loadConfiguration() throws ConfigurationException;

}

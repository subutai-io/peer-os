package org.safehaus.subutai.configuration.manager.impl.utils;


import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.YamlConfigurationLoader;
import org.apache.cassandra.exceptions.ConfigurationException;


/**
 * Created by bahadyr on 7/9/14.
 */
public class CassandraYamlConfigLoader implements ConfigurationLoader {
    @Override
    public Object loadConfiguration() throws ConfigurationException {

        YamlConfigurationLoader y = new YamlConfigurationLoader();
        Config c = y.loadConfig();
        System.out.println(c.seed_provider);

        return null;
    }
}

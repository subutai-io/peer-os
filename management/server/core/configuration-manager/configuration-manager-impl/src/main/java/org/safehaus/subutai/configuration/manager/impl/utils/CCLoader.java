package org.safehaus.subutai.configuration.manager.impl.utils;


import org.apache.cassandra.config.YamlConfigurationLoader;


/**
 * Created by bahadyr on 7/9/14.
 */
public class CCLoader extends YamlConfigurationLoader {


    public CCLoader() {
        System.setProperty( "cassandra.config", "cassandra.2.0.4/cassandra.yaml" );

    }
}

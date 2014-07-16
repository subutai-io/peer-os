package org.safehaus.subutai.configuration.manager.impl.utils;


import org.yaml.snakeyaml.Yaml;


/**
 * Created by bahadyr on 7/9/14.
 */
public class YamConfigurationlLoader implements ConfigurationLoader {


    /*public YamlLoader() {
        System.setProperty( "cassandra.config", "cassandra.2.0.5/cassandra.yaml" );
    }*/

    public Object loadConfig( ) {
        Yaml yaml = new Yaml();
        Object result = yaml.loadAs( "", Object.class );
        return result;
    }


    @Override
    public Object getConfiguration() {
        return null;
    }
}

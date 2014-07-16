package org.safehaus.subutai.configuration.manager.impl.utils;


import org.safehaus.subutai.configuration.manager.api.Config;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.shared.protocol.Agent;
import org.yaml.snakeyaml.Yaml;


/**
 * Created by bahadyr on 7/9/14.
 */
public class YamConfigurationlLoader implements ConfigurationLoader {


    @Override
    public Config getConfiguration( final Agent agent, final String configPathFilename ) {

        //TODO cat file from given agent, convert to required format, detect types and form a Config
        Yaml yaml = new Yaml();
        Object result = yaml.loadAs( configPathFilename, Object.class );

        Config config = new Config();
        config.setPath( configPathFilename );
        config.setConfigTypeEnum( ConfigTypeEnum.YAML );

        // TODO iterate through yaml to set Config field values
        return null;
    }


    @Override
    public void setConfiguration( final Agent agent, Config config ) {
        // TODO Read config from instance
        ConfigurationInjector injector = new ConfigurationInjector();
        String content = injector.catFile( agent, config.getPath() );

        // TODO set values to yaml object from Config
        Yaml yaml = new Yaml();
        Object result = yaml.loadAs( content, Object.class );

        String newContent = ""; // yaml to string

        // TODO inject Config
        injector.echoTextIntoAgent( agent, config.getPath(), newContent );
    }
}

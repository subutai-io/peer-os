package org.safehaus.subutai.configuration.manager.impl.loaders;


import org.safehaus.subutai.configuration.manager.impl.command.TextInjectorImpl;
import org.safehaus.subutai.shared.protocol.Agent;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/9/14.
 */
public class YamConfigurationlLoader implements ConfigurationLoader {


    @Override
    public JsonObject getConfiguration( final Agent agent, final String configPathFilename ) {

        //TODO cat file from given agent, convert to required format, detect types and form a Config
        Yaml yaml = new Yaml();
        Object result = yaml.loadAs( configPathFilename, Object.class );

        //
        JsonObject jsonObject = new JsonObject();

        // TODO iterate through yaml to set Config field values
        return jsonObject;
    }


    @Override
    public boolean setConfiguration( final Agent agent, String configFilePath, String config ) {
        // TODO Read config from instance
        TextInjectorImpl injector = new TextInjectorImpl();
        String content = injector.catFile( agent, "" );

        // TODO set values to yaml object from Config
        Yaml yaml = new Yaml();
        Object result = yaml.loadAs( content, Object.class );

        String newContent = ""; // yaml to string

        // TODO inject Config
        injector.echoTextIntoAgent( agent, "path", newContent );
        return true;
    }
}

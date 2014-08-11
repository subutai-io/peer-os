package org.safehaus.subutai.configuration.manager.impl.utils;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 8/4/14.
 */
public class ShellParser implements ConfigParser {

    private String content;


    public ShellParser( final String content ) {
        this.content = content;
    }


    @Override
    public JsonObject parserConfig( final String pathToConfig, final ConfigTypeEnum configTypeEnum ) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        JsonObject jo = configBuilder.getConfigJsonObject( pathToConfig, configTypeEnum );

        List<JsonObject> fields = new ArrayList<>();
        String key = "plain";
        String value = content;
        JsonObject field =
                configBuilder.buildFieldJsonObject( key.trim(), key.trim(), true, "textfield", true, value.trim() );
        fields.add( field );

        JsonObject njo = configBuilder.addJsonArrayToConfig( jo, fields );

        return njo;
    }
}

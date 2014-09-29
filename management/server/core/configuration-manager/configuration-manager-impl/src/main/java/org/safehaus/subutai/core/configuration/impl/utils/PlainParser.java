package org.safehaus.subutai.core.configuration.impl.utils;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.configuration.api.ConfiguraitonTypeEnum;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 8/4/14.
 */
public class PlainParser implements ConfigParser
{

    private String content;


    public PlainParser( final String content )
    {
        this.content = content;
    }


    @Override
    public JsonObject parserConfig( final String pathToConfig, final ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        ConfigBuilder configBuilder = new ConfigBuilder();
        JsonObject jo = configBuilder.getConfigJsonObject( pathToConfig, configuraitonTypeEnum );

        List<JsonObject> fields = new ArrayList<>();
        String key = "plain";
        String value = content;
        JsonObject field =
                configBuilder.buildFieldJsonObject( key.trim(), key.trim(), true, "textfield", true, value.trim() );
        fields.add( field );

        return configBuilder.addJsonArrayToConfig( jo, fields );
    }
}

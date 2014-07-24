package org.safehaus.subutai.configuration.manager.impl.loaders;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.command.TextInjectorImpl;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigBuilder;
import org.safehaus.subutai.configuration.manager.impl.utils.IniParser;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoader implements ConfigurationLoader {

    //    String filename = "/home/bahadyr/Desktop/products/hadoop-1.2.1/conf/log4j.properties";


    // Maps configuration into Config object
    @Override
    public JsonObject getConfiguration( Agent agent, String configPathFilename ) {

        TextInjectorImpl configurationInjector = new TextInjectorImpl();
        String content = configurationInjector.catFile( agent, configPathFilename );

        try {
            IniParser iniParser = null;
            iniParser = new IniParser( content );
            PropertiesConfiguration propertiesConfiguration = iniParser.getConfig();
            Iterator<String> iterator = propertiesConfiguration.getKeys();

            ConfigBuilder configBuilder = new ConfigBuilder();
            JsonObject jo = configBuilder.getConfigJsonObject( configPathFilename, ConfigTypeEnum.PROPERTIES );
            List<JsonObject> fields = new ArrayList<>();
            while ( iterator.hasNext() ) {
                String key = iterator.next();
                String value = iniParser.getStringProperty( key );
                JsonObject field = configBuilder.buildFieldJsonObject( key, "", true, "", true, value );
                fields.add( field );
            }
            JsonObject cjo = configBuilder.addJsonArrayToConfig( jo, fields );
            return cjo;
            //            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            //            System.out.println( gson.toJson( cjo ) );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean setConfiguration( final Agent agent, String configFilePath, String jsonObjectConfig ) {
        //TODO Read config from instance, set values from Config, inject Config
        //        JsonObject jsonObject = getConfiguration( agent, "" );
        TextInjectorImpl configurationInjector = new TextInjectorImpl();
        String content = configurationInjector.catFile( agent, configFilePath );
        Gson gson = new Gson();

        try {
            IniParser iniParser = new IniParser( content );
            JsonObject config = gson.fromJson( jsonObjectConfig, JsonObject.class );
            JsonArray jsonArray2 = config.getAsJsonArray( "configFields" );
            for ( int i = 0; i < jsonArray2.size(); i++ ) {
                JsonObject jo1 = ( JsonObject ) jsonArray2.get( i );
                String fieldName = jo1.getAsJsonPrimitive( "fieldName" ).getAsString();
                String value = jo1.getAsJsonPrimitive( "value" ).getAsString();
                iniParser.setProperty( fieldName, value );
            }

            configurationInjector.echoTextIntoAgent( agent, configFilePath, content );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void convertConfig( String configText ) {
        try {
            IniParser iniParser = new IniParser( "" );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
    }
}

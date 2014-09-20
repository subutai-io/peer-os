package org.safehaus.subutai.core.configuration.impl.loaders;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;
import org.safehaus.subutai.core.configuration.api.TextInjector;
import org.safehaus.subutai.core.configuration.impl.command.TextInjectorImpl;
import org.safehaus.subutai.core.configuration.impl.utils.ConfigBuilder;
import org.safehaus.subutai.core.configuration.impl.utils.IniParser;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoader implements ConfigurationLoader
{

    private static final Logger LOG = Logger.getLogger( PropertiesConfigurationLoader.class.getName() );
    private TextInjector textInjector;


    public PropertiesConfigurationLoader( final TextInjector textInjector )
    {
        this.textInjector = textInjector;
    }


    @Override
    public JsonObject getConfiguration( String hostname, String configPathFilename )
    {

        TextInjectorImpl configurationInjector = new TextInjectorImpl();
        String content = configurationInjector.catFile( hostname, configPathFilename );

        try
        {
            IniParser iniParser = null;
            iniParser = new IniParser( content );
            PropertiesConfiguration propertiesConfiguration = iniParser.getConfig();
            Iterator<String> iterator = propertiesConfiguration.getKeys();

            ConfigBuilder configBuilder = new ConfigBuilder();
            JsonObject jo = configBuilder.getConfigJsonObject( configPathFilename, ConfigTypeEnum.PROPERTIES );
            List<JsonObject> fields = new ArrayList<>();
            while ( iterator.hasNext() )
            {
                String key = iterator.next();
                String value = iniParser.getStringProperty( key );
                JsonObject field = configBuilder.buildFieldJsonObject( key, "", true, "", true, value );
                fields.add( field );
            }
            return configBuilder.addJsonArrayToConfig( jo, fields );
        }
        catch ( ConfigurationException e )
        {
            LOG.log( Level.SEVERE, e.getMessage() );
        }
        return null;
    }


    @Override
    public boolean setConfiguration( final String hostname, String configFilePath, String jsonObjectConfig )
    {
        // TODO Read config from instance, set values from Config, inject Config

        String content = textInjector.catFile( hostname, configFilePath );
        Gson gson = new Gson();

        try
        {
            IniParser iniParser = new IniParser( content );
            JsonObject config = gson.fromJson( jsonObjectConfig, JsonObject.class );
            JsonArray jsonArray2 = config.getAsJsonArray( "configFields" );
            for ( int i = 0; i < jsonArray2.size(); i++ )
            {
                JsonObject jo1 = ( JsonObject ) jsonArray2.get( i );
                String fieldName = jo1.getAsJsonPrimitive( "fieldName" ).getAsString();
                String value = jo1.getAsJsonPrimitive( "value" ).getAsString();
                iniParser.setProperty( fieldName, value );
            }

            textInjector.echoTextIntoAgent( hostname, configFilePath, content );
            return true;
        }
        catch ( ConfigurationException e )
        {
            LOG.log( Level.SEVERE, e.getMessage() );
        }
        return false;
    }
}

package org.safehaus.subutai.core.configuration.cli;


import org.safehaus.subutai.core.configuration.api.ConfigurationManager;
import org.safehaus.subutai.core.configuration.api.ConfiguraitonTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


/**
 * Displays the last log entries
 */
@Command( scope = "config", name = "get-config-json",
        description = "Gets the json of given configuration tempalte" )
public class GetConfigJsonCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( GetConfigJsonCommand.class );
    @Argument( index = 0, name = "pathToFile", required = true, multiValued = false, description = "Path to file" )
    String pathToFile;
    @Argument( index = 1, name = "type", required = true, multiValued = false, description = "Configuration type" )
    String type;

    private ConfigurationManager configurationManager;


    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }


    public void setConfigurationManager( final ConfigurationManager configurationManager )
    {
        this.configurationManager = configurationManager;
    }


    protected Object doExecute()
    {

        JsonObject jsonObject = configurationManager.getJsonObjectFromResources( pathToFile, ConfiguraitonTypeEnum
                .valueOf( type ) );

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOG.info( "GetConfigJsonCommand@doExecute: " + gson.toJson( jsonObject ) );

        return null;
    }
}

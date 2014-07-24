package org.safehaus.subutai.cli.commands;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

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
public class GetConfigJsonCommand extends OsgiCommandSupport {

    @Argument( index = 0, name = "pathToFile", required = true, multiValued = false, description = "Path to file" )
    String pathToFile;
    @Argument( index = 1, name = "type", required = true, multiValued = false, description = "Configuration type" )
    String type;

    private ConfigManager configManager;


    public ConfigManager getConfigManager() {
        return configManager;
    }


    public void setConfigManager( final ConfigManager configManager ) {
        this.configManager = configManager;
    }


    protected Object doExecute() {

        JsonObject jsonObject = configManager.getJsonObjectFromResources( pathToFile, ConfigTypeEnum.valueOf( type ) );

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println( gson.toJson( jsonObject ) );


        return null;
    }
}

package org.safehaus.subutai.core.configuration.cli;


import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "config", name = "echo-text-to-file",
        description = "Echo text into a file")
public class EchoTextToFileCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "Path to file")
    String hostname;
    @Argument(index = 1, name = "json", required = true, multiValued = false, description = "Path to file")
    String json;
    @Argument(index = 2, name = "pathToFile", required = true, multiValued = false, description = "Path to file")
    String pathToFile;
    @Argument(index = 3, name = "type", required = true, multiValued = false, description = "Configuration type")
    String type;

    //    private AgentManager agentManager;
    private ConfigManager configManager;


    //    public AgentManager getAgentManager() {
    //        return agentManager;
    //    }


    //    public void setAgentManager( final AgentManager agentManager ) {
    //        this.agentManager = agentManager;
    //    }


    public ConfigManager getConfigManager()
    {
        return configManager;
    }


    public void setConfigManager( final ConfigManager configManager )
    {
        this.configManager = configManager;
    }


    protected Object doExecute()
    {

        //        Agent agent = agentManager.getAgentByHostname( hostname );

        boolean result =
                configManager.injectConfiguration( hostname, pathToFile, json, ConfigTypeEnum.valueOf( type ) );

        System.out.println( result );


        return null;
    }
}

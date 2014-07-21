package org.safehaus.subutai.configuration.manager.api;


import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 7/19/14.
 */
public interface TextInjector {

    public boolean echoTextIntoAgent( Agent agent, String pathToFile, String content );

    /**
     * Gets the content of the given file on given agent.
     * @param agent
     * @param pathToFile
     * @return
     */
    public String catFile( Agent agent, String pathToFile );

    public String getConfigTemplate(String path);
}

package org.safehaus.subutai.configuration.manager.impl.utils;


import org.safehaus.subutai.configuration.manager.api.Config;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 7/16/14.
 */
public interface ConfigurationLoader {

    public Config getConfiguration( Agent agent, String configPathFilename );

    public void setConfiguration( Agent agent, Config config );
}

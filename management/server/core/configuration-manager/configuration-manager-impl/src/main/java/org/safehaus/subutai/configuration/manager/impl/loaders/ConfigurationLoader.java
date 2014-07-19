package org.safehaus.subutai.configuration.manager.impl.loaders;


import org.safehaus.subutai.shared.protocol.Agent;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public interface ConfigurationLoader {

    public JsonObject getConfiguration( Agent agent, String configPathFilename );

    public void setConfiguration( Agent agent, JsonObject config );
}

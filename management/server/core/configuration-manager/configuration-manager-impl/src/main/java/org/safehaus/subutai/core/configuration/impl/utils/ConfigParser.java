package org.safehaus.subutai.core.configuration.impl.utils;


import com.google.gson.JsonObject;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;


/**
 * Created by bahadyr on 7/21/14.
 */
public interface ConfigParser {
	JsonObject parserConfig(String pathToConfig, ConfigTypeEnum configTypeEnum);

//    public void setProperty( String path, String value );

//    public Object getProperty( String path );
}

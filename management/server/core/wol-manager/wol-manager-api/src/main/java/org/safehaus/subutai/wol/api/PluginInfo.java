package org.safehaus.subutai.wol.api;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginInfo
{
    public String getVersion();

    public void setVersion( String version );

    public String getPluginName();

    public void setPluginName( String pluginName );

    public String getType();

    public void setType( String type );

    public String getRating();

    public void setRating( String rating );

}

package org.safehaus.subutai.pluginmanager.api;


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

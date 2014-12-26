package org.safehaus.subutai.pluginmanager.impl;


import org.safehaus.subutai.pluginmanager.api.PluginInfo;


public class PluginInfoImpl implements PluginInfo
{
    private String type;
    private String pluginName;
    private String version;
    private String rating;



    /*public PluginInfoImpl ( String pluginName, String packageName, String packageVersion )
    {
        this.pluginName = pluginName;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
    }*/


    @Override
    public String getVersion()
    {
        return version;
    }


    @Override
    public void setVersion( final String version )
    {
        this.version = version;
    }


    @Override
    public String getPluginName()
    {
        return pluginName;
    }


    @Override
    public void setPluginName( final String pluginName )
    {
        this.pluginName = pluginName;
    }


    @Override
    public String getType()
    {
        return type;
    }


    @Override
    public void setType( final String type )
    {
        this.type = type;
    }


    @Override
    public String getRating()
    {
        return rating;
    }


    @Override
    public void setRating( final String rating )
    {
        this.rating = rating;
    }
}

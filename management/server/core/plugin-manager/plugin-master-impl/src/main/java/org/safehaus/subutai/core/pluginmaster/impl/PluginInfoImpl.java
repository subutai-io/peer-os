package org.safehaus.subutai.core.pluginmaster.impl;


import org.safehaus.subutai.core.pluginmaster.api.PluginInfo;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginInfoImpl implements PluginInfo
{
    private String pluginName;
    private String packageName;
    private String packageVersion;


    @Override
    public String getPackageName()
    {
        return packageName;
    }


    @Override
    public void setPackageName( final String packageName )
    {
        this.packageName = packageName;
    }


    @Override
    public String getPackageVersion()
    {
        return packageVersion;
    }


    @Override
    public void setPackageVersion( final String version )
    {
        this.packageVersion = version;
    }


    @Override
    public String getPluginName()
    {
        return null;
    }


    @Override
    public void setPluginName( final String pluginName )
    {
        this.pluginName = pluginName;
    }
}

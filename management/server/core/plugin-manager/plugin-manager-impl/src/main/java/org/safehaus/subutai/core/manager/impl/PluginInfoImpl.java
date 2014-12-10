package org.safehaus.subutai.core.manager.impl;


import org.safehaus.subutai.core.manager.api.PluginInfo;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginInfoImpl implements PluginInfo
{
    private String pluginName;
    private String packageName;
    private String packageVersion;
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


    @Override
    public void setPackageName( final String packageName )
    {
        this.packageName = packageName;
    }


    @Override
    public String getPackageName()
    {
        return packageName;
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
}

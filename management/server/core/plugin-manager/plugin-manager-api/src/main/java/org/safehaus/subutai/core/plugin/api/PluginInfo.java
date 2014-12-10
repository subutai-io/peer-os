package org.safehaus.subutai.core.plugin.api;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginInfo
{
    public String getPackageName();

    public void setPackageName(String packageName);

    public String getPackageVersion();

    public void setPackageVersion( String version );

    public String getPluginName();

    public void setPluginName( String pluginName );


}

package org.safehaus.subutai.core.plugin.api;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginInfo
{
    public String getPluginName();

    public String setPluginName(String pluginName);

    public String setPackageName(String packageName);

    public String getPackageName();

    public String getPackageVersion();

    public String setPackageVersion(String version);
}

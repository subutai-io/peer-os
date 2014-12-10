package org.safehaus.subutai.core.manager.api;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginInfo
{
    public void setPackageVersion(String version);

    public void setPluginName(String pluginName);

    public void setPackageName(String packageName);

    public String getPackageName();

    public String getPackageVersion();

    public String getPluginName();


}

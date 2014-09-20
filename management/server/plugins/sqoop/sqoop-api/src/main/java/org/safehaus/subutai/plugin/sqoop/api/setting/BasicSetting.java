package org.safehaus.subutai.plugin.sqoop.api.setting;


public class BasicSetting
{

    String clusterName;
    String hostname;


    public String getClusterName()
    {
        return clusterName;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( String hostname )
    {
        this.hostname = hostname;
    }


    @Override
    public String toString()
    {
        return "clusterName=" + clusterName + ", hostname=" + hostname;
    }
}

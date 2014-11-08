package org.safehaus.subutai.plugin.spark.rest;


import java.util.Set;


public class TrimmedSparkConfig
{

    private String clusterName;
    private String hadoopClusterName;

    private String masterNodeHostName;
    private Set<String> slavesHostName;


    public String getClusterName()
    {
        return clusterName;
    }


    public String getMasterNodeHostName()
    {
        return masterNodeHostName;
    }


    public Set<String> getSlavesHostName()
    {
        return slavesHostName;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }
}

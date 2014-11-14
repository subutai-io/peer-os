package org.safehaus.subutai.plugin.presto.rest;


import java.util.Set;


/**
 * Created by talas on 9/8/14.
 */
public class TrimmedPrestoConfig
{
    private String clusterName;
    private String hadoopClusterName;

    private String coordinatorHost;
    private Set<String> workersHost;


    public String getClusterName()
    {
        return clusterName;
    }


    public String getCoordinatorHost()
    {
        return coordinatorHost;
    }


    public Set<String> getWorkersHost()
    {
        return workersHost;
    }

    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }
}

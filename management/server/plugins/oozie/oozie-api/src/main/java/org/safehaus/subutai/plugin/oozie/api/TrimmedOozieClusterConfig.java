package org.safehaus.subutai.plugin.oozie.api;


/**
 * Created by bahadyr on 9/4/14.
 */
public class TrimmedOozieClusterConfig
{

    private String hadoopClusterName;
    private String serverHostname;
    private int numberOfCluents;
    private String clusterName;


    public String getClusterName()
    {
        return clusterName;
    }


    public void setClusterName( final String clusterName )
    {
        this.clusterName = clusterName;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( final String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public String getServerHostname()
    {
        return serverHostname;
    }


    public void setServerHostname( final String serverHostname )
    {
        this.serverHostname = serverHostname;
    }


    public int getNumberOfCluents()
    {
        return numberOfCluents;
    }


    public void setNumberOfCluents( final int numberOfCluents )
    {
        this.numberOfCluents = numberOfCluents;
    }
}

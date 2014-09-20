package org.safehaus.subutai.plugin.spark.impl.mock;


import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class SparkImplMock extends SparkImpl
{

    private SparkClusterConfig clusterConfig;

    //    public SparkImplMock() {
    //        super( mock( CommandRunner.class ), mock( AgentManager.class ), mock( DbManager.class ),
    // new TrackerMock() );
    //    }


    public SparkImplMock setClusterConfig( SparkClusterConfig clusterConfig )
    {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public SparkClusterConfig getCluster( String clusterName )
    {
        return clusterConfig;
    }
}

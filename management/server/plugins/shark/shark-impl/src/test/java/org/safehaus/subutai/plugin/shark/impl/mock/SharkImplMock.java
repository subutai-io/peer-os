package org.safehaus.subutai.plugin.shark.impl.mock;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.Spark;

import static org.mockito.Mockito.mock;


public class SharkImplMock extends SharkImpl
{
    private SharkClusterConfig clusterConfig;


    public SharkImplMock()
    {
        super( mock( CommandRunner.class ), mock( AgentManager.class ), mock( DbManager.class ), new TrackerMock(),
                mock( Spark.class ), mock( Hadoop.class ), mock( EnvironmentManager.class ) );
    }


    public SharkImplMock setClusterConfig( SharkClusterConfig clusterConfig )
    {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public SharkClusterConfig getCluster( String clusterName )
    {
        return clusterConfig;
    }
}


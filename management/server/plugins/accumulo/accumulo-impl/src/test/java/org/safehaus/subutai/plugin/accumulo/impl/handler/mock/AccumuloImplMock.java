package org.safehaus.subutai.plugin.accumulo.impl.handler.mock;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
//import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
//import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
//import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
//import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

import static org.mockito.Mockito.mock;


public class AccumuloImplMock extends AccumuloImpl {

    private AccumuloClusterConfig clusterAccumuloClusterConfig = null;


    public AccumuloImplMock() {
//        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
//                mock( Hadoop.class ), mock( Zookeeper.class ), mock( EnvironmentManager.class ));
        super ( mock( CommandRunner.class ), mock( AgentManager.class ), mock( DbManager.class ), MockBuilder.getTrackerMock(),
                mock( Hadoop.class ), mock( Zookeeper.class ), mock( EnvironmentManager.class ) );
    }


    public AccumuloImplMock setClusterAccumuloClusterConfig( AccumuloClusterConfig clusterAccumuloClusterConfig ) {
        this.clusterAccumuloClusterConfig = clusterAccumuloClusterConfig;
        return this;
    }

    public AccumuloImplMock setCommands( Commands commands ) {
        this.commands = commands;
        return this;
    }

    public void setAccumuloClusterConfig( AccumuloClusterConfig config ){
        this.clusterAccumuloClusterConfig = config;
    }


    @Override
    public AccumuloClusterConfig getCluster( String clusterName ) {
        return clusterAccumuloClusterConfig;
    }


    public AccumuloImplMock setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
        return this;
    }
}

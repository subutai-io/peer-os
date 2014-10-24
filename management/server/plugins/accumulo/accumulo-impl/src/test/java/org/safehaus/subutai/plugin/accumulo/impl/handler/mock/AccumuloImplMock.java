package org.safehaus.subutai.plugin.accumulo.impl.handler.mock;


import javax.sql.DataSource;

import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;

import static org.mockito.Mockito.mock;


public class AccumuloImplMock extends AccumuloImpl
{

    private AccumuloClusterConfig clusterAccumuloClusterConfig = null;


    public AccumuloImplMock()
    {
        super( mock( DataSource.class ) );
    }


    public AccumuloImplMock setClusterAccumuloClusterConfig( AccumuloClusterConfig clusterAccumuloClusterConfig )
    {
        this.clusterAccumuloClusterConfig = clusterAccumuloClusterConfig;
        return this;
    }


    public AccumuloImplMock setCommands( Commands commands )
    {
        this.commands = commands;
        return this;
    }


    public void setAccumuloClusterConfig( AccumuloClusterConfig config )
    {
        this.clusterAccumuloClusterConfig = config;
    }


    @Override
    public AccumuloClusterConfig getCluster( String clusterName )
    {
        return clusterAccumuloClusterConfig;
    }

    //
    //    public AccumuloImplMock setAgentManager( AgentManager agentManager )
    //    {
    //        this.agentManager = agentManager;
    //        return this;
    //    }
}

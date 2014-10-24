package org.safehaus.subutai.plugin.sqoop.impl.mock;


import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SqoopImplMock extends SqoopImpl
{

    private SqoopConfig sqoopConfig = null;


    public static TrackerOperation getProductOperationMock()
    {
        return mock( TrackerOperation.class );
    }


    public SqoopImplMock()
    {
        super( mock( DataSource.class ) );
        setCommandRunner( mock( CommandRunner.class ) );
        setAgentManager( mock( AgentManager.class ) );
        setTracker( new TrackerMock() );
    }


    public SqoopImpl getSqoopImplMock()
    {
        SqoopImpl simp = mock( SqoopImpl.class );
        when( simp.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        sqoopConfig = new SqoopConfig();
        return simp;
    }


    public SqoopImpl getSqoopImplConfiguredMock()
    {
        SqoopImpl simp = mock( SqoopImpl.class );
        when( simp.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        when( simp.getCluster( anyString() ) ).thenReturn( new SqoopConfig() );
        sqoopConfig = new SqoopConfig();
        return simp;
    }


    public SqoopImpl getSqoopImplConfigurationNotBelongToSqoopNodes()
    {
        SqoopImpl simp = mock( SqoopImpl.class );
        when( simp.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        when( simp.getCluster( anyString() ) ).thenReturn( new SqoopConfig() );
        when( simp.getAgentManager().getAgentByHostname( anyString() ) ).thenReturn( mock( Agent.class ) );
        sqoopConfig = new SqoopConfig();
        return simp;
    }


    @Override
    public SqoopConfig getCluster( String clusterName )
    {
        return sqoopConfig;
    }


    public SqoopConfig getSqoopConfig()
    {
        return sqoopConfig;
    }


    public SqoopImplMock setSqoopConfig( SqoopConfig sqoopConfig )
    {
        this.sqoopConfig = sqoopConfig;
        return this;
    }
}

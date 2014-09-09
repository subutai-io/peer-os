package org.safehaus.subutai.plugin.sqoop.impl.mock;

import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SqoopImplMock extends SqoopImpl {

    private SqoopConfig sqoopConfig = null;

    public SqoopImpl getSqoopImplMock(){
        SqoopImpl simp = mock( SqoopImpl.class );
        when( simp.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        sqoopConfig = new SqoopConfig();
        return simp;
    }

    public static ProductOperation getProductOperationMock(){
        return mock( ProductOperation.class );
    }


    public SqoopImplMock setSqoopConfig( SqoopConfig sqoopConfig ) {
        this.sqoopConfig = sqoopConfig;
        return this;
    }

    @Override
    public SqoopConfig getCluster( String clusterName ) {
        return sqoopConfig;
    }

    public SqoopConfig getSqoopConfig() {
        return sqoopConfig;
    }
}

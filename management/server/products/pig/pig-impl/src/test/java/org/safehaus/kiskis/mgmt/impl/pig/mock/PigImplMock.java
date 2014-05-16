package org.safehaus.kiskis.mgmt.impl.pig.mock;


import org.safehaus.kiskis.mgmt.api.pig.Config;
import org.safehaus.kiskis.mgmt.impl.pig.PigImpl;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.TrackerMock;


public class PigImplMock extends PigImpl {

    private Config clusterConfig = null;

    public PigImplMock() {
        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock() );
    }


    public PigImplMock setClusterConfig( Config clusterConfig ) {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public Config getCluster( String clusterName ) {
        return clusterConfig;
    }
}

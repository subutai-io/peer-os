package org.safehaus.kiskis.mgmt.impl.solr.mock;


import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.LxcManagerMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.TrackerMock;


public class SolrImplMock extends SolrImpl {

    public SolrImplMock() {
        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
                new LxcManagerMock() );
    }


    public SolrImplMock setCommands( Commands commands ) {
        this.commands = commands;
        return this;
    }

}

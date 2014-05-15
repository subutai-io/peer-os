package org.safehaus.kiskis.mgmt.impl.solr.util;


import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;


public class SolrImplMock extends SolrImpl {

    public SolrImplMock() {
        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
                new LxcManagerMock() );
    }


    public SolrImplMock setCommands(Commands commands) {
        this.commands = commands;
        return this;
    }

}

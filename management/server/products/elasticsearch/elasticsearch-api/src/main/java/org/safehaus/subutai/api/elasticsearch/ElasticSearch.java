package org.safehaus.subutai.api.elasticsearch;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.api.commandrunner.AgentResult;

public interface Elasticsearch {

    public AgentResult install( Agent agent );

}

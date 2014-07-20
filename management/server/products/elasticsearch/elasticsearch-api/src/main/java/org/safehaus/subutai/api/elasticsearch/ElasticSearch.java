package org.safehaus.subutai.api.elasticsearch;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.api.commandrunner.AgentResult;

public interface Elasticsearch {

    public AgentResult install( Agent agent );

    public AgentResult remove( Agent agent );

    public AgentResult serviceStart( Agent agent );

    public AgentResult serviceStop( Agent agent );

    public AgentResult serviceStatus( Agent agent );

}

package org.safehaus.subutai.common.protocol.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Created by talas on 10/29/14.
 */
public interface AgentService
{
    public Agent getAgent( long id );

    public List<Agent> getAgents();

    public void deleteAgent( long id );

    public Agent createAgent( Agent agent );
}

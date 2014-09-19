package org.safehaus.subutai.common.util;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Provides utility functions for working with agents
 */
public class AgentUtil
{

    public static String getAgentIpByMask( Agent agent, String mask )
    {
        if ( agent != null )
        {
            if ( agent.getListIP() != null && !agent.getListIP().isEmpty() )
            {
                for ( String ip : agent.getListIP() )
                {
                    if ( ip.matches( mask ) )
                    {
                        return ip;
                    }
                }
            }
            return agent.getHostname();
        }
        return null;
    }


    public static Set<Agent> filterLxcAgents( Set<Agent> agents )
    {
        Set<Agent> filteredAgents = new HashSet<>();
        if ( agents != null )
        {
            for ( Agent agent : agents )
            {
                if ( agent.isLXC() )
                {
                    filteredAgents.add( agent );
                }
            }
        }
        return filteredAgents;
    }


    public static Set<Agent> filterPhysicalAgents( Set<Agent> agents )
    {
        Set<Agent> filteredAgents = new HashSet<>();
        if ( agents != null )
        {
            for ( Agent agent : agents )
            {
                if ( !agent.isLXC() )
                {
                    filteredAgents.add( agent );
                }
            }
        }
        return filteredAgents;
    }
}

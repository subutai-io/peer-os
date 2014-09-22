package org.safehaus.subutai.core.agent.impl;


import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentListener;


/**
 * Runnable to notify listeners about change in connected agents set
 */
public class AgentNotifier implements Runnable
{
    private static final Logger LOG = Logger.getLogger( AgentNotifier.class.getName() );

    private final AgentManagerImpl agentManager;


    public AgentNotifier( final AgentManagerImpl agentManager )
    {
        this.agentManager = agentManager;
    }


    public void run()
    {
        long lastNotify = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            try
            {
                if ( agentManager.isNotifyAgentListeners()
                        || System.currentTimeMillis() - lastNotify > Common.AGENT_FRESHNESS_MIN * 60 * 1000 / 2 )
                {
                    lastNotify = System.currentTimeMillis();
                    agentManager.setNotifyAgentListeners( false );
                    Set<Agent> freshAgents = agentManager.getAgents();
                    for ( Iterator<AgentListener> it = agentManager.listeners.iterator(); it.hasNext(); )
                    {
                        AgentListener listener = it.next();
                        try
                        {
                            listener.onAgent( freshAgents );
                        }
                        catch ( Exception e )
                        {
                            it.remove();
                            LOG.log( Level.SEVERE, "Error notifying agent listeners, removing faulting listener", e );
                        }
                    }
                }
                Thread.sleep( 1000L );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
        }
    }
}

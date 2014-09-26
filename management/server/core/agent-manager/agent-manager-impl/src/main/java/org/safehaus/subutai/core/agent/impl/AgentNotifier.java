package org.safehaus.subutai.core.agent.impl;


import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runnable to notify listeners about change in connected agents set
 */
public class AgentNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( AgentNotifier.class.getName() );

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
                    notifyListeners( agentManager.getAgents() );
                }
                Thread.sleep( 1000L );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
        }
    }


    private void notifyListeners( Set<Agent> agents )
    {
        for ( Iterator<AgentListener> it = agentManager.getListenersQueue().iterator(); it.hasNext(); )
        {
            AgentListener listener = it.next();
            try
            {
                listener.onAgent( agents );
            }
            catch ( Exception e )
            {
                it.remove();
                LOG.error( "Error notifying agent listeners, removing faulting listener", e );
            }
        }
    }
}

package org.safehaus.subutai.core.filetracker.impl;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.filetracker.api.FileTracker;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Needed mostly for testing FileTracker
 */
@Command(scope = "file-tracker", name = "test")
public class CliTest extends OsgiCommandSupport implements ResponseListener
{

    private static final String[] CONFIG_POINTS = new String[] { "/etc", "/etc/ksks-agent" };

    private AgentManager agentManager;

    private FileTracker fileTracker;


    public void setAgentManager( AgentManager agentManager )
    {
        Preconditions.checkNotNull( agentManager, "AgentManager is null." );
        this.agentManager = agentManager;
    }


    public void setFileTracker( FileTracker fileTracker )
    {
        Preconditions.checkNotNull( fileTracker, "FileTracker is null." );
        this.fileTracker = fileTracker;
    }


    protected Object doExecute()
    {

        fileTracker.addListener( this );

        Agent agent = getAgent();

        fileTracker.createConfigPoints( agent, CONFIG_POINTS );

        return null;
    }


    private Agent getAgent()
    {

        for ( Agent agent : agentManager.getAgents() )
        {
            if ( "management".equals( agent.getHostname() ) )
            {
                return agent;
            }
        }

        return null;
    }


    @Override
    public void onResponse( Response response )
    {
        //some dummy method
    }
}

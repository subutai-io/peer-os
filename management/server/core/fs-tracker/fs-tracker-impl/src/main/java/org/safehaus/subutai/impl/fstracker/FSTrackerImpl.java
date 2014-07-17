package org.safehaus.subutai.impl.fstracker;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.fstracker.FileTracker;
import org.safehaus.subutai.api.fstracker.FileTrackerListener;


public class FSTrackerImpl implements FileTracker {

    private AgentManager agentManager;


    public void init() {
        System.out.printf( "init" );
    }


    public void destroy() {
        System.out.printf( "destroy" );
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    @Override
    public void addListener( final FileTrackerListener listener ) {
        System.out.println( "addListener()" );
    }
}

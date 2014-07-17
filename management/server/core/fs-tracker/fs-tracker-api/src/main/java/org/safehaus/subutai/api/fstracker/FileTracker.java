package org.safehaus.subutai.api.fstracker;


import org.safehaus.subutai.shared.protocol.Agent;


public interface FileTracker {

    public void addListener( FileTrackerListener listener );

    public void createConfigPoints( Agent agent, String configPoints[] );

    public void removeConfigPoints( Agent agent, String configPoints[] );

    public String[] listConfigPoints( Agent agent );

}

package org.safehaus.subutai.core.filetracker.api;


import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.peer.api.Host;


public interface FileTracker
{

    public void addListener( ResponseListener listener );

    public void removeListener( ResponseListener listener );

    public void createConfigPoints( Host host, String[] configPoints ) throws FileTrackerException;

    public void removeConfigPoints( Host host, String[] configPoints ) throws FileTrackerException;

    public String[] listConfigPoints( Host host ) throws FileTrackerException;
}

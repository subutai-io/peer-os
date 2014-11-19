package org.safehaus.subutai.core.filetracker.api;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.Host;


public interface FileTracker
{

    public void addListener( ConfigPointListener listener );

    public void removeListener( ConfigPointListener listener );

    public void createConfigPoints( Host host, Set<String> configPoints ) throws FileTrackerException;

    public void removeConfigPoints( Host host, Set<String> configPoints ) throws FileTrackerException;

    public Set<String> listConfigPoints( Host host ) throws FileTrackerException;
}
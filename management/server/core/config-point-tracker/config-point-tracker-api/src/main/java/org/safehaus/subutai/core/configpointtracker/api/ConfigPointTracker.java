package org.safehaus.subutai.core.configpointtracker.api;


import java.util.Set;


public interface ConfigPointTracker
{

    public boolean add( String templateName, String... configPaths );

    public boolean remove( String templateName, String... configPaths );

    public Set<String> get( String templateName );
}

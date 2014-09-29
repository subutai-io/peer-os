package org.safehaus.subutai.core.configpointtracker.api;


import java.util.Set;


public interface ConfigPointTracker
{

    public void add( String templateName, String... configPaths );

    public void remove( String templateName, String... configPaths );

    public Set<String> get( String templateName );
}

package org.safehaus.subutai.core.configpointtracker.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.core.configpointtracker.api.ConfigPointTracker;


public class ConfigPointTrackerImpl implements ConfigPointTracker
{

    // <templateName, configPoints>
    private final Map<String, Set<String>> configPoints = new HashMap<>();


    @Override
    public boolean add( String templateName, String... configPaths )
    {
        boolean result = true;
        Set<String> points = configPoints.get( templateName );

        if ( points == null )
        {
            points = new HashSet<>();
            configPoints.put( templateName, points );
            result = false;
        }

        Collections.addAll( points, configPaths );
        return result;
    }


    @Override
    public boolean remove( String templateName, String... configPaths )
    {

        Set<String> points = configPoints.get( templateName );

        if ( points == null )
        {
            return false;
        }

        for ( String path : configPaths )
        {
            points.remove( path );
        }
        return true;
    }


    @Override
    public Set<String> get( String templateName )
    {
        return configPoints.get( templateName );
    }
}

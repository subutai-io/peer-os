package org.safehaus.subutai.impl.configpointtracker;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.configpointtracker.ConfigPointTracker;


public class ConfigPointTrackerImpl implements ConfigPointTracker {

    // <templateName, configPoints>
    private final HashMap<String, Set<String>> configPoints = new HashMap<>();


    @Override
    public void add( String templateName, String... configPaths ) {

        Set<String> points = configPoints.get( templateName );

        if ( points == null ) {
            points = new HashSet<>();
            configPoints.put( templateName, points );
        }

        for ( String path : configPaths ) {
            points.add( path );
        }
    }
}

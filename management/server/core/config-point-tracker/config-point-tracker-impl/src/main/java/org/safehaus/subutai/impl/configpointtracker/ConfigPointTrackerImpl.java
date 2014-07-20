package org.safehaus.subutai.impl.configpointtracker;


import java.util.HashMap;
import java.util.Set;

import org.safehaus.subutai.api.configpointtracker.ConfigPointTracker;


public class ConfigPointTrackerImpl implements ConfigPointTracker {

    // <templateName, configPoints>
    private final HashMap<String, Set<String>> templateConfPoints = new HashMap<>();


    @Override
    public void add( String templateName, String... configPoints ) {

    }
}

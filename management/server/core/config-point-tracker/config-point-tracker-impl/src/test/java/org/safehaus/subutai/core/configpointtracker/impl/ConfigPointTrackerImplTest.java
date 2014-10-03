package org.safehaus.subutai.core.configpointtracker.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * Created by talas on 10/3/14.
 */
public class ConfigPointTrackerImplTest
{

    private ConfigPointTrackerImpl configPointTracker;
    private String templateName = "templateName";


    @Before
    public void setupClasses()
    {
        configPointTracker = new ConfigPointTrackerImpl();
    }


    @Test
    public void shouldAddTemplateOnAdd()
    {
        boolean result = configPointTracker.add( templateName, "templateConfig" );
        assertEquals( true, result );
    }


    // <templateName, configPoints>
    private final Map<String, Set<String>> configPoints = new HashMap<>();


    @Test
    public void shouldRemoveFromMapTemplateNameOnRemove()
    {
        boolean result = configPointTracker.remove( templateName, "configPaths" );
        assertEquals( false, result );
    }


    @Test
    public void shouldGetTemplateNamesAsSetOnGet()
    {
        configPointTracker.add( templateName, templateName );
        Set<String> templateNames = configPointTracker.get( templateName );
        assertArrayEquals( templateNames.toArray(), Sets.newHashSet( templateName ).toArray() );
    }
}

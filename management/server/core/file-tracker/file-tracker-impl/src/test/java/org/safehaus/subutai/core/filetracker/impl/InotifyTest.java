package org.safehaus.subutai.core.filetracker.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.common.util.UUIDUtil;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;


public class InotifyTest
{
    private final static UUID ID = UUIDUtil.generateRandomUUID();
    private static final Set<String> CONFIG_POINTS = Sets.newHashSet( "/etc/approx", "/etc/nginx" );
    private static final String INOTIFY_RESPONSE = String.format(
            "{ \"response\": {" + "  \"type\": \"INOTIFY_EVENT\"," + "  \"id\": \"%s\","
                    + "  \"configPoints\":[%s]  } }", ID, StringUtil.joinStrings( CONFIG_POINTS, ',', true ) );


    @Test
    public void testProperties() throws Exception
    {
        InotifyEvent inotifyEvent = JsonUtil.fromJson( INOTIFY_RESPONSE, InotifyEvent.class );

        assertEquals( ID, inotifyEvent.getResponse().getId() );
        assertEquals( CONFIG_POINTS, inotifyEvent.getResponse().getConfigPoints() );
    }
}
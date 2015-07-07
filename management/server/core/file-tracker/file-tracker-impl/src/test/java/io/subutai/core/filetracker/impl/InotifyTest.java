package io.subutai.core.filetracker.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import io.subutai.core.filetracker.api.InotifyEventType;
import io.subutai.core.filetracker.impl.InotifyEvent;

import static org.junit.Assert.assertEquals;


public class InotifyTest
{
    private static final  UUID ID = UUIDUtil.generateRandomUUID();
    private static final String CONFIG_POINT = "/etc/approx";
    private static final InotifyEventType EVENT_TYPE= InotifyEventType.CREATE_FOLDER;
    private static final String INOTIFY_RESPONSE = String.format(
            "{ \"response\": {" + "  \"type\": \"INOTIFY_EVENT\"," + "  \"id\": \"%s\"," + "  \"configPoint\":\"%s\", \"dateTime\":\"18.11.2014 11:42:39\", \"eventType\":\"%s\"  } }",
            ID, CONFIG_POINT,EVENT_TYPE );


    @Test
    public void testProperties() throws Exception
    {
        InotifyEvent inotifyEvent = JsonUtil.fromJson( INOTIFY_RESPONSE, InotifyEvent.class );

        assertEquals( ID, inotifyEvent.getResponse().getId() );
        assertEquals( CONFIG_POINT, inotifyEvent.getResponse().getConfigPoint() );
        assertEquals( EVENT_TYPE, inotifyEvent.getResponse().getEventType() );
    }
}
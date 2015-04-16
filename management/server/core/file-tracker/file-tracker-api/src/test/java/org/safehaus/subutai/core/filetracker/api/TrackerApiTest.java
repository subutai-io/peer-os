package org.safehaus.subutai.core.filetracker.api;


import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class TrackerApiTest
{

    @Test
    public void testInotifyEventTypeEnum() throws Exception
    {
        assertTrue( InotifyEventType.valueOf( "CREATE_FOLDER" ) == InotifyEventType.CREATE_FOLDER );
    }


    @Test
    public void testMessageException() throws Exception
    {
        Exception nestedException = new Exception();

        FileTrackerException messageException = new FileTrackerException( nestedException );

        assertEquals( nestedException, messageException.getCause() );
    }
}

package io.subutai.core.appender;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiErrorEventTest
{
    private SubutaiErrorEvent subutaiErrorEvent;


    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final String LOGGER = "LOGGER";
    private static final String ERR_MSG = "ERROR";
    private static final String ERR_STACKTRACE = "ERROR\nCAUSE";


    @Before
    public void setUp() throws Exception
    {
        subutaiErrorEvent = new SubutaiErrorEvent( TIMESTAMP, LOGGER, ERR_MSG, ERR_STACKTRACE );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TIMESTAMP, subutaiErrorEvent.getTimeStamp() );
        assertEquals( LOGGER, subutaiErrorEvent.getLoggerName() );
        assertEquals( ERR_MSG, subutaiErrorEvent.getRenderedMessage() );
        assertEquals( ERR_STACKTRACE, subutaiErrorEvent.getStackTrace() );
    }
}

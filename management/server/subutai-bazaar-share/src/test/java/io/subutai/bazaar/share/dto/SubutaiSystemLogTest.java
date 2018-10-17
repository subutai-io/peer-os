package io.subutai.bazaar.share.dto;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiSystemLogTest
{
    private SubutaiSystemLog subutaiSystemLog;


    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final SubutaiSystemLog.LogSource SOURCE = SubutaiSystemLog.LogSource.PEER;
    private static final String SOURCE_NAME = "PEER1";
    private static final String LOGGER = "LOGGER";
    private static final String ERR_MSG = "ERROR";
    private static final String ERR_STACKTRACE = "ERROR\nCAUSE";


    @Before
    public void setUp() throws Exception
    {
        subutaiSystemLog =
                new SubutaiSystemLog( SOURCE, SOURCE_NAME, SubutaiSystemLog.LogType.ERROR, TIMESTAMP, LOGGER, ERR_MSG,
                        ERR_STACKTRACE );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TIMESTAMP, subutaiSystemLog.getTimeStamp() );
        assertEquals( SOURCE, subutaiSystemLog.getSourceType() );
        assertEquals( SOURCE_NAME, subutaiSystemLog.getSourceName() );
        assertEquals( LOGGER, subutaiSystemLog.getLoggerName() );
        assertEquals( ERR_MSG, subutaiSystemLog.getRenderedMessage() );
        assertEquals( ERR_STACKTRACE, subutaiSystemLog.getStackTrace() );
    }
}

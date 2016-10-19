package io.subutai.core.appender;


import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiAppenderTest
{
    @Mock
    Set<SubutaiErrorEventListener> listeners;
    @Mock
    ExecutorService notifierPool;
    @Mock
    SubutaiErrorEventListener listener;
    @Mock
    LoggingEvent loggingEvent;
    @Mock
    ThrowableInformation throwableInformation;
    @Mock Throwable throwable;

    private SubutaiAppender subutaiAppender;


    @Before
    public void setUp() throws Exception
    {
        subutaiAppender = spy( new SubutaiAppender() );
        subutaiAppender.notifierPool = notifierPool;
        subutaiAppender.listeners = listeners;
    }


    @Test
    public void testDispose() throws Exception
    {
        subutaiAppender.dispose();

        verify( listeners ).clear();
    }


    @Test
    public void testRequiresLayout() throws Exception
    {

        assertFalse( subutaiAppender.requiresLayout() );
    }


    @Test
    public void testAddRemoveListener() throws Exception
    {
        subutaiAppender.addListener( listener );

        verify( listeners ).add( listener );

        subutaiAppender.removeListener( listener );

        verify( listeners ).remove( listener );
    }


    @Test
    public void testAppend() throws Exception
    {

        doReturn( SubutaiAppender.MIN_REPORT_LOG_LEVEL ).when( loggingEvent ).getLevel();
        doReturn( throwableInformation ).when( loggingEvent ).getThrowableInformation();
        doReturn( throwable ).when( throwableInformation ).getThrowable();
        subutaiAppender.listeners = Sets.newSet(listener);

        subutaiAppender.append( loggingEvent );

        verify( notifierPool ).execute( isA( Runnable.class ) );
    }
}

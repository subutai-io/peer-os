package io.subutai.core.test.appender;


import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;


public class SolAppender extends AppenderSkeleton
{

    final static int MAX_EVENTS_IN_QUEUE = 100;
    final static Deque<LoggingEvent> loggingEvents = new LinkedBlockingDeque<>();


    public static Set<String> getLoggingEvents()
    {

        Set<String> events = new HashSet<>();
        for ( LoggingEvent loggingEvent : loggingEvents )
        {
            events.add( loggingEvent.getRenderedMessage() );
        }
        loggingEvents.clear();
        return events;
    }


    @Override
    protected synchronized void append( final LoggingEvent event )
    {
        if ( loggingEvents.size() == MAX_EVENTS_IN_QUEUE )
        {
            loggingEvents.removeFirst();
        }

        loggingEvents.add( event );
    }


    @Override
    public void close()
    {

    }


    @Override
    public boolean requiresLayout()
    {
        return false;
    }
}

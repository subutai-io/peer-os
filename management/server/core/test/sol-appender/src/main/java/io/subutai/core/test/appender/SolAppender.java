package io.subutai.core.test.appender;


import java.io.PrintWriter;
import java.io.StringWriter;
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


    public static Set<SubutaiLogEvent> getLoggingEvents()
    {
        Set<SubutaiLogEvent> events = new HashSet<>();
        for ( LoggingEvent loggingEvent : loggingEvents )
        {
            String stacktrace = null;
            if ( loggingEvent.getThrowableInformation() != null
                    && loggingEvent.getThrowableInformation().getThrowable() != null )
            {
                StringWriter errors = new StringWriter();
                loggingEvent.getThrowableInformation().getThrowable().printStackTrace( new PrintWriter( errors ) );
                stacktrace = errors.toString();
            }
            events.add( new SubutaiLogEvent( loggingEvent.getTimeStamp(), loggingEvent.getMessage(),
                    loggingEvent.getLoggerName(), loggingEvent.getRenderedMessage(), stacktrace ) );
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


    public static class SubutaiLogEvent
    {
        final long timeStamp;
        final Object message;
        final String loggerName;
        final String renderedMessage;
        final String fullInfo;


        public SubutaiLogEvent( final long timeStamp, final Object message, final String loggerName,
                                final String renderedMessage, final String fullInfo )
        {
            this.timeStamp = timeStamp;
            this.message = message;
            this.loggerName = loggerName;
            this.renderedMessage = renderedMessage;
            this.fullInfo = fullInfo;
        }


        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder( "SubutaiLogEvent{" );
            sb.append( "timeStamp=" ).append( timeStamp );
            sb.append( ", message=" ).append( message );
            sb.append( ", loggerName='" ).append( loggerName ).append( '\'' );
            sb.append( ", renderedMessage='" ).append( renderedMessage ).append( '\'' );
            sb.append( ", fullInfo='" ).append( fullInfo ).append( '\'' );
            sb.append( '}' );
            return sb.toString();
        }
    }
}

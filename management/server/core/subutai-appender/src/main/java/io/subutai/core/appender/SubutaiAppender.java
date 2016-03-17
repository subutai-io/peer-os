package io.subutai.core.appender;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;


public class SubutaiAppender extends AppenderSkeleton
{

    private static final Level MIN_REPORT_LOG_LEVEL = Level.ERROR;
    protected static Set<SubutaiErrorEventListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<SubutaiErrorEventListener, Boolean>() );

    protected ExecutorService notifierPool = Executors.newCachedThreadPool();


    public void dispose()
    {
        listeners.clear();
    }


    @Override
    protected void append( final LoggingEvent event )
    {
        try
        {
            if ( event.getLevel().isGreaterOrEqual( MIN_REPORT_LOG_LEVEL ) && event.getThrowableInformation() != null
                    && event.getThrowableInformation().getThrowable() != null )
            {
                StringWriter errors = new StringWriter();
                event.getThrowableInformation().getThrowable().printStackTrace( new PrintWriter( errors ) );
                final String stacktrace = errors.toString();

                for ( final SubutaiErrorEventListener listener : listeners )
                {
                    notifierPool.execute( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                listener.onEvent( new SubutaiErrorEvent( event.getTimeStamp(), event.getLoggerName(),
                                        event.getRenderedMessage(), stacktrace ) );
                            }
                            catch ( Exception e )
                            {
                                //ignore to exclude cycling
                            }
                        }
                    } );
                }
            }
        }
        catch ( Exception e )
        {
            //ignore
        }
    }


    public void addListener( SubutaiErrorEventListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void removeListener( SubutaiErrorEventListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
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

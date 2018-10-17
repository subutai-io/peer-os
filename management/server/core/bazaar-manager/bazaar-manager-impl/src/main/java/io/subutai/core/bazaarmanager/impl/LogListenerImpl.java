package io.subutai.core.bazaarmanager.impl;


import java.security.MessageDigest;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.ArrayUtils;

import io.subutai.core.appender.SubutaiErrorEvent;
import io.subutai.core.appender.SubutaiErrorEventListener;


public class LogListenerImpl implements SubutaiErrorEventListener
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Map<String, SubutaiErrorEvent> errLogs = new LinkedHashMap<>();


    public Set<SubutaiErrorEvent> getSubutaiErrorEvents()
    {
        Set<SubutaiErrorEvent> logs = new HashSet<>();

        synchronized ( errLogs )
        {
            logs.addAll( errLogs.values() );
            errLogs.clear();
        }

        return logs;
    }


    @Override
    public void onEvent( final SubutaiErrorEvent event )
    {
        log.info( String.format( "RECEIVED:%n:%s", event.toString() ) );

        try
        {
            byte[] loggerName = event.getLoggerName().getBytes();
            byte[] renderedMsg = event.getRenderedMessage().getBytes();
            byte[] combined = ArrayUtils.addAll( loggerName, renderedMsg );
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            byte[] theDigest = md.digest( combined );
            String key = new String( theDigest );

            synchronized ( errLogs )
            {
                errLogs.put( key, event );

                while ( errLogs.size() > 10 )
                {
                    //delete oldest value
                    errLogs.remove( errLogs.keySet().iterator().next() );
                }
            }
        }
        catch ( Exception e )
        {
            log.warn( "Error in #onEvent {}", e.getMessage() );
        }
    }
}

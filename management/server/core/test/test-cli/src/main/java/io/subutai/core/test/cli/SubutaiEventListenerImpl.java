package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.appender.SubutaiErrorEvent;
import io.subutai.core.appender.SubutaiErrorEventListener;


public class SubutaiEventListenerImpl implements SubutaiErrorEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( SubutaiEventListenerImpl.class.getName() );


    @Override
    public void onEvent( final SubutaiErrorEvent event )
    {
        LOG.info( String.format( "RECEIVED:%n:%s", event.toString() ) );
    }
}

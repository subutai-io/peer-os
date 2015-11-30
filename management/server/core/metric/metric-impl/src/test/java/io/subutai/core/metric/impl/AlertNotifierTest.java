package io.subutai.core.metric.impl;


import org.junit.Test;

import io.subutai.common.metric.Alert;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.core.metric.api.AlertListener;

import org.slf4j.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for AlertNotifier
 */
public class AlertNotifierTest
{
    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMetric() throws Exception
    {

        new AlertNotifier( null, mock( AlertListener.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullListener() throws Exception
    {

        new AlertNotifier( mock( Alert.class ), null );
    }


    @Test
    public void testListenerShouldBeNotified() throws Exception
    {

        Alert alert = mock( Alert.class );
        AlertListener listener = mock( AlertListener.class );
        AlertNotifier alertNotifier = new AlertNotifier( alert, listener );

        alertNotifier.run();

        verify( listener ).onAlert( alert );
    }


    @Test
    public void testLogError() throws Exception
    {
        Alert alert = mock( Alert.class );
        AlertListener listener = mock( AlertListener.class );
        Logger logger = mock( Logger.class );
        doThrow( new RuntimeException( "" ) ).when( listener ).onAlert( alert );
        AlertNotifier alertNotifier = new AlertNotifier( alert, listener );
        alertNotifier.LOG = logger;

        alertNotifier.run();

        verify( logger ).error( anyString(), any( RuntimeException.class ) );
    }
}

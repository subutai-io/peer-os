package org.safehaus.subutai.core.metric.impl;


import org.junit.Test;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
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

        new AlertNotifier( null, mock( MetricListener.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullListener() throws Exception
    {

        new AlertNotifier( mock( ContainerHostMetric.class ), null );
    }


    @Test
    public void testListenerShouldBeNotified() throws Exception
    {

        ContainerHostMetric metric = mock( ContainerHostMetric.class );
        MetricListener listener = mock( MetricListener.class );
        AlertNotifier alertNotifier = new AlertNotifier( metric, listener );

        alertNotifier.run();

        verify( listener ).alertThresholdExcess( metric );
    }


    @Test
    public void testLogError() throws Exception
    {
        ContainerHostMetric metric = mock( ContainerHostMetric.class );
        MetricListener listener = mock( MetricListener.class );
        Logger logger = mock( Logger.class );
        doThrow( new RuntimeException( "" ) ).when( listener ).alertThresholdExcess( metric );
        AlertNotifier alertNotifier = new AlertNotifier( metric, listener );
        alertNotifier.LOG = logger;

        alertNotifier.run();

        verify( logger ).error( anyString(), any( RuntimeException.class ) );
    }
}

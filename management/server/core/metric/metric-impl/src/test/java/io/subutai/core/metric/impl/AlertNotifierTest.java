package io.subutai.core.metric.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import io.subutai.common.metric.AbstractAlert;
import io.subutai.common.metric.Alert;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for AlertNotifier
 */
@RunWith( MockitoJUnitRunner.class )
public class AlertNotifierTest
{
    private static final String TEMPLATE_NAME = "master";
    private static final String RESOURCE_ID = "resource_id";

    @Mock
    AlertPack alert;

    @Mock
    AbstractAlert resource;

    @Mock
    AlertListener listener;

    @Before
    public void setUp() {
        when( resource.getId() ).thenReturn( RESOURCE_ID );
        when( alert.getTemplateName() ).thenReturn( TEMPLATE_NAME );
        when( alert.getResource() ).thenReturn( resource );
    }

    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMetric() throws Exception
    {

        new AlertNotifier( null, mock( AlertListener.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullListener() throws Exception
    {
        new AlertNotifier( alert, null );
    }


    @Test
    public void testListenerShouldBeNotified() throws Exception
    {

        AlertNotifier alertNotifier = new AlertNotifier( alert, listener );

        alertNotifier.run();

        verify( listener ).onAlert( alert );
    }


    @Test
    public void testLogError() throws Exception
    {
        Logger logger = mock( Logger.class );
        doThrow( new RuntimeException( "" ) ).when( listener ).onAlert( alert );
        AlertNotifier alertNotifier = new AlertNotifier( alert, listener );
        alertNotifier.LOG = logger;

        alertNotifier.run();

        verify( logger ).error( anyString(), any( RuntimeException.class ) );
    }
}

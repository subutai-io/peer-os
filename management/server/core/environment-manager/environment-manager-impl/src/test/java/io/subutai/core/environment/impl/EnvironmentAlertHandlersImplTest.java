package io.subutai.core.environment.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentId;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * Environment alert handlers test
 */
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentAlertHandlersImplTest
{
    private static final String ID_LOW = "name_low";
    private static final String ID_NORMAL = "name_normal";
    private static final String ID_HIGH = "name_high";
    private static final String ID_NULL = "name_null";
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    @Mock
    EnvironmentAlertHandler lowId;
    @Mock
    EnvironmentAlertHandler normalId;
    @Mock
    EnvironmentAlertHandler highId;

    @Mock
    EnvironmentAlertHandler nullHandlerId;

    @Mock
    AlertHandler lowHandler;
    @Mock
    AlertHandler normalHandler;
    @Mock
    AlertHandler highHandler;

    @Mock
    EnvironmentId environmentId;

    EnvironmentAlertHandlers environmentAlertHandlers;


    @Before
    public void setUp()
    {
        when( environmentId.getId() ).thenReturn( ENVIRONMENT_ID );
        when( lowId.getAlertHandlerId() ).thenReturn( ID_LOW );
        when( normalId.getAlertHandlerId() ).thenReturn( ID_NORMAL );
        when( highId.getAlertHandlerId() ).thenReturn( ID_HIGH );
        when( nullHandlerId.getAlertHandlerId() ).thenReturn( ID_NULL );

        when( nullHandlerId.getAlertHandlerPriority() ).thenReturn( AlertHandlerPriority.NORMAL );
        when( lowId.getAlertHandlerPriority() ).thenReturn( AlertHandlerPriority.LOW );
        when( normalId.getAlertHandlerPriority() ).thenReturn( AlertHandlerPriority.NORMAL );
        when( highId.getAlertHandlerPriority() ).thenReturn( AlertHandlerPriority.HIGH );

        when( lowHandler.getId() ).thenReturn( ID_LOW );
        when( normalHandler.getId() ).thenReturn( ID_NORMAL );
        when( highHandler.getId() ).thenReturn( ID_HIGH );

        environmentAlertHandlers = new EnvironmentAlertHandlersImpl( environmentId );
    }


    @Test
    public void testConstructor()
    {
        assertEquals( environmentId, environmentAlertHandlers.getEnvironmentId() );
    }


    @Test
    public void testGetAllHandlers()
    {
        environmentAlertHandlers.add( lowId, lowHandler);
        environmentAlertHandlers.add( highId, highHandler );
        environmentAlertHandlers.add( normalId, normalHandler);
        environmentAlertHandlers.add( nullHandlerId, null );

        final Map<EnvironmentAlertHandler, AlertHandler> allHandlers = environmentAlertHandlers.getAllHandlers();
        final Iterator<EnvironmentAlertHandler> i = allHandlers.keySet().iterator();
        assertEquals( 4, allHandlers.size() );
        EnvironmentAlertHandler id = i.next();
        assertEquals( highId, id );
        id = i.next();
        assertEquals( normalId, id );
        id = i.next();
        assertEquals( nullHandlerId, id );
        id = i.next();
        assertEquals( lowId, id );

        final Iterator<AlertHandler> i2 = allHandlers.values().iterator();
        assertEquals( 4, allHandlers.size() );
        AlertHandler h = i2.next();
        assertEquals( highHandler, h );
        h = i2.next();
        assertEquals( normalHandler, h );
        h = i2.next();
        assertEquals( null, h );
        h = i2.next();
        assertEquals( lowHandler, h );
    }


    @Test
    public void testGetEffectiveHandlers()
    {
        environmentAlertHandlers.add( normalId, normalHandler );
        environmentAlertHandlers.add( highId, highHandler );
        environmentAlertHandlers.add( lowId, lowHandler );
        environmentAlertHandlers.add( nullHandlerId, null );

        final Collection<AlertHandler> allHandlers = environmentAlertHandlers.getEffectiveHandlers();

        final Iterator<AlertHandler> i2 = allHandlers.iterator();
        assertEquals( 3, allHandlers.size() );
        AlertHandler h = i2.next();
        assertEquals( highHandler, h );
        h = i2.next();
        assertEquals( normalHandler, h );
        h = i2.next();
        assertEquals( lowHandler, h );
    }
}

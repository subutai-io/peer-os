package io.subutai.core.environment.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.core.environment.impl.entity.EnvironmentAlertHandlerImpl;

import static org.junit.Assert.assertEquals;


/**
 * Alert Handler identifier test
 */
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentAlertHandlerImplTest
{
    private static final String NAME_LOW = "name_low";
    private static final String NAME_NORMAL = "name_normal";
    private static final String NAME_NORMAL2 = "name_normal2";
    private static final String NAME_HIGH = "name_high";
    EnvironmentAlertHandlerImpl low;
    EnvironmentAlertHandlerImpl normal;
    EnvironmentAlertHandlerImpl normal2;
    EnvironmentAlertHandlerImpl high;


    @Before
    public void setUp()
    {
        low = new EnvironmentAlertHandlerImpl( NAME_LOW, AlertHandlerPriority.LOW );
        normal = new EnvironmentAlertHandlerImpl( NAME_NORMAL, AlertHandlerPriority.NORMAL );
        normal2 = new EnvironmentAlertHandlerImpl( NAME_NORMAL2, AlertHandlerPriority.NORMAL );
        high = new EnvironmentAlertHandlerImpl( NAME_HIGH, AlertHandlerPriority.HIGH );
    }


    @Test
    public void testConstructor()
    {
        assertEquals( AlertHandlerPriority.LOW, low.getAlertHandlerPriority() );
        assertEquals( NAME_LOW, low.getAlertHandlerId() );
    }


    @Test
    public void testSort()
    {
        List<EnvironmentAlertHandlerImpl> list = new ArrayList<>();
        list.add( normal2 );
        list.add( high );
        list.add( low );
        list.add( normal );

        Collections.sort( list );

        assertEquals( high, list.get( 0 ) );
        assertEquals( normal, list.get( 1 ) );
        assertEquals( normal2, list.get( 2 ) );
        assertEquals( low, list.get( 3 ) );
    }
}

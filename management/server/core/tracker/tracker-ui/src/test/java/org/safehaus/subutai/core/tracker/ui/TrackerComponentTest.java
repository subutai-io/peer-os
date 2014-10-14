package org.safehaus.subutai.core.tracker.ui;


import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.core.tracker.impl.ProductOperationImpl;
import org.safehaus.subutai.core.tracker.impl.ProductOperationViewImpl;
import org.safehaus.subutai.core.tracker.impl.TrackerImpl;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for TrackerComponent
 */
@RunWith( MockitoJUnitRunner.class )
public class TrackerComponentTest
{

    @Mock
    Tracker tracker;
    @Mock
    ExecutorService executorService;
    private static final String SOURCE = "source";
    private static final String DESCRIPTION = "description";
    private static final UUID OPERATION_ID = UUID.randomUUID();
    private TrackerComponent trackerComponent;
    private ProductOperationImpl po;


    @Before
    public void setUp() throws Exception
    {
        trackerComponent = new TrackerComponent( tracker, executorService );
        po = new ProductOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );
        final ProductOperationView productOperationView = new ProductOperationViewImpl( po );
        when( tracker.getProductOperation( SOURCE, OPERATION_ID ) ).thenReturn( productOperationView );
        when( tracker.getProductOperationSources() ).thenReturn( Lists.newArrayList( SOURCE ) );
        when( tracker.getProductOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() ) )
                .thenReturn( Lists.newArrayList( productOperationView ) );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullTracker() throws Exception
    {
        new TrackerComponent( null, executorService );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullExecutor() throws Exception
    {
        new TrackerComponent( tracker, null );
    }


    @Test
    public void shouldExecuteRunnable() throws Exception
    {
        ExecutorService executorService = mock( ExecutorService.class );
        TrackerComponent trackerComponent = new TrackerComponent( tracker, executorService );

        trackerComponent.startTracking();

        verify( executorService ).execute( any( Runnable.class ) );
        assertTrue( trackerComponent.isTrack() );
    }


    @Test
    public void shouldPollProductOperationSources() throws Exception
    {
        Tracker tracker = mock( Tracker.class );
        TrackerComponent trackerComponent = new TrackerComponent( tracker, executorService );

        trackerComponent.refreshSources();

        verify( tracker ).getProductOperationSources();
    }


    @Test
    public void testPopulateOperations() throws Exception
    {


        trackerComponent.refreshSources();
        trackerComponent.setTrackID( OPERATION_ID );
        trackerComponent.populateOperations();
        trackerComponent.populateLogs();


        verify( tracker ).getProductOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() );
    }


    @Test
    public void testPopulateOperations2() throws Exception
    {

        trackerComponent.refreshSources();
        po.addLogDone( "" );
        when( tracker.getProductOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() ) )
                .thenReturn( Lists.<ProductOperationView>newArrayList( new ProductOperationViewImpl( po ) ) );

        trackerComponent.populateOperations();

        verify( tracker ).getProductOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() );
    }


    @Test
    public void testPopulateOperations3() throws Exception
    {
        trackerComponent.refreshSources();
        po.addLogFailed( "" );
        when( tracker.getProductOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() ) )
                .thenReturn( Lists.<ProductOperationView>newArrayList( new ProductOperationViewImpl( po ) ) );

        trackerComponent.populateOperations();

        verify( tracker ).getProductOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() );
    }
}

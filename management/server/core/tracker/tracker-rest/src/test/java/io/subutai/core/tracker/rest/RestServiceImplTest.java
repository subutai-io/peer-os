package io.subutai.core.tracker.rest;


import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.common.util.JsonUtil;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.tracker.impl.TrackerImpl;
import io.subutai.core.tracker.impl.TrackerOperationImpl;
import io.subutai.core.tracker.impl.TrackerOperationViewImpl;
import io.subutai.core.tracker.rest.RestServiceImpl;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for RestServiceImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    @Mock
    Tracker tracker;
    private static final String SOURCE = "source";
    private static final String DESCRIPTION = "description";
    private static final UUID OPERATION_ID = UUID.randomUUID();
    private static final UUID OPERATION_ID2 = UUID.randomUUID();

    private RestServiceImpl restService;


    @Before
    public void setUp() throws Exception
    {

        restService = new RestServiceImpl( tracker );
        final TrackerOperationView trackerOperationView = new TrackerOperationViewImpl(
                new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) ) );
        when( tracker.getTrackerOperation( SOURCE, OPERATION_ID ) ).thenReturn( trackerOperationView );
        when( tracker.getTrackerOperationSources() ).thenReturn( Collections.<String>emptyList() );
        when( tracker.getTrackerOperations( anyString(), any( Date.class ), any( Date.class ), anyInt() ) )
                .thenReturn( Lists.newArrayList( trackerOperationView ) );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullTracker() throws Exception
    {
        new RestServiceImpl( null );
    }


    @Test
    public void testGetProductOperation() throws Exception
    {

        Response response = restService.getTrackerOperation( SOURCE, OPERATION_ID.toString() );

        TrackerOperationView pov = JsonUtil.fromJson( response.getEntity().toString(), TrackerOperationViewImpl.class );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertNotNull( pov );
    }


    @Test
    public void testGetProductOperationNotFound() throws Exception
    {

        Response response = restService.getTrackerOperation( SOURCE, OPERATION_ID2.toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetProductOperationException() throws Exception
    {

        Response response = restService.getTrackerOperation( SOURCE, null );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetProductOperationSources() throws Exception
    {
        Response response = restService.getTrackerOperationSources();

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetProductOperations() throws Exception
    {
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        Type listPOVType = new TypeToken<List<TrackerOperationViewImpl>>()
        {
        }.getType();


        Response response =
                restService.getTrackerOperations( SOURCE, df.format( new Date() ), df.format( new Date() ), 1 );
        List<TrackerOperationView> pov = JsonUtil.fromJson( response.getEntity().toString(), listPOVType );


        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertTrue( !pov.isEmpty() );
    }


    @Test
    public void testGetProductOperationsException() throws Exception
    {
        Response response = restService.getTrackerOperations( SOURCE, null, "", 1 );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.util.JsonUtil;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for TrackerImpl class
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class TrackerImplTest
{
    @Mock
    ResultSet resultSet;
    @Mock
    Clob clobInfo;
    TrackerImpl tracker;
    private static final String SOURCE = "source";
    private static final String DESCRIPTION = "description";
    private static final String DONE = "done";
    private static final UUID operationId = UUID.randomUUID();

    private TrackerOperationImpl productOperation;


    private ByteArrayOutputStream myOut;


    @After
    public void tearDown() throws Exception
    {
        System.setOut( System.out );
    }


    private String getSysOut()
    {
        return myOut.toString().trim();
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullDataSource() throws Exception
    {
        //        new TrackerImpl( (DataSource)null );
    }


    @Test
    public void testGetProductOperation() throws Exception
    {
        TrackerOperationView pv = tracker.getTrackerOperation( SOURCE, operationId );

        assertNotNull( pv );
    }


    @Test
    public void testGetProductOperationException() throws Exception
    {
        //when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        TrackerOperationView pv = tracker.getTrackerOperation( SOURCE, operationId );

        assertNull( pv );
    }


    @Test
    public void testSaveProductOperation() throws Exception
    {
        assertTrue( tracker.saveTrackerOperation( SOURCE, productOperation ) );
    }


    @Test
    public void testSaveProductOperationException() throws Exception
    {
        //when( dbUtil.update( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        assertFalse( tracker.saveTrackerOperation( SOURCE, productOperation ) );
    }


    @Test
    public void testCreateProductOperation() throws Exception
    {
        TrackerOperation po = tracker.createTrackerOperation( SOURCE, DESCRIPTION );

        assertNotNull( po );
    }


    @Test
    public void testCreateProductOperationException() throws Exception
    {

        //when( dbUtil.update( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        TrackerOperation po = tracker.createTrackerOperation( SOURCE, DESCRIPTION );

        assertNull( po );
    }


    @Test
    public void testGetProductOperations() throws Exception
    {

        List<TrackerOperationView> pos = tracker.getTrackerOperations( SOURCE, new Date(), new Date(), 1 );

        assertNotNull( pos );
        assertFalse( pos.isEmpty() );
    }


    @Test
    public void testGetProductOperationsException() throws Exception
    {
        //when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        List<TrackerOperationView> pos = tracker.getTrackerOperations( SOURCE, new Date(), new Date(), 1 );

        assertNotNull( pos );
        assertTrue( pos.isEmpty() );
    }


    @Test
    public void testGetProductOperationSources() throws Exception
    {
        List<String> sources = tracker.getTrackerOperationSources();

        assertNotNull( sources );
        assertFalse( sources.isEmpty() );
    }


    @Test
    public void testGetProductOperationSourcesException() throws Exception
    {
        //when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        List<String> sources = tracker.getTrackerOperationSources();

        assertNotNull( sources );
        assertTrue( sources.isEmpty() );
    }


    @Test
    public void testPrintOperationLog() throws Exception
    {
        productOperation.addLogDone( DONE );
        when( clobInfo.getSubString( anyLong(), anyInt() ) ).thenReturn( JsonUtil.toJson( productOperation ) );

        tracker.printOperationLog( SOURCE, operationId, 1000 );

        assertThat( getSysOut(), containsString( DONE ) );
    }
}

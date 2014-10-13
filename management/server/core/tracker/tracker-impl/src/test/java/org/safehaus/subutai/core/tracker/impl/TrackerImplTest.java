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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationView;
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

@RunWith( MockitoJUnitRunner.class )
public class TrackerImplTest
{

    @Mock
    DataSource dataSource;
    @Mock
    DbUtil dbUtil;
    @Mock
    ResultSet resultSet;
    @Mock
    Clob clobInfo;
    TrackerImpl tracker;
    private static final String SOURCE = "source";
    private static final String DESCRIPTION = "description";
    private static final String DONE = "done";
    private static final UUID operationId = UUID.randomUUID();

    private ProductOperationImpl productOperation;


    private ByteArrayOutputStream myOut;


    @Before
    public void setUp() throws SQLException
    {
        productOperation = new ProductOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );
        tracker = new TrackerImplExt( dataSource, dbUtil );
        when( dbUtil.select( anyString(), anyVararg() ) ).thenReturn( resultSet );
        when( resultSet.next() ).thenReturn( true ).thenReturn( false );
        when( resultSet.getClob( "info" ) ).thenReturn( clobInfo );
        when( resultSet.getString( SOURCE ) ).thenReturn( SOURCE );
        when( clobInfo.length() ).thenReturn( 1L );

        when( clobInfo.getSubString( anyLong(), anyInt() ) ).thenReturn( JsonUtil.toJson( productOperation ) );

        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }


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
        new TrackerImpl( null );
    }


    @Test
    public void testGetProductOperation() throws Exception
    {
        ProductOperationView pv = tracker.getProductOperation( SOURCE, operationId );

        assertNotNull( pv );
    }


    @Test
    public void testGetProductOperationException() throws Exception
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        ProductOperationView pv = tracker.getProductOperation( SOURCE, operationId );

        assertNull( pv );
    }


    @Test
    public void testSaveProductOperation() throws Exception
    {
        assertTrue( tracker.saveProductOperation( SOURCE, productOperation ) );
    }


    @Test
    public void testSaveProductOperationException() throws Exception
    {
        when( dbUtil.update( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        assertFalse( tracker.saveProductOperation( SOURCE, productOperation ) );
    }


    @Test
    public void testCreateProductOperation() throws Exception
    {
        ProductOperation po = tracker.createProductOperation( SOURCE, DESCRIPTION );

        assertNotNull( po );
    }


    @Test
    public void testCreateProductOperationException() throws Exception
    {

        when( dbUtil.update( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        ProductOperation po = tracker.createProductOperation( SOURCE, DESCRIPTION );

        assertNull( po );
    }


    @Test
    public void testGetProductOperations() throws Exception
    {

        List<ProductOperationView> pos = tracker.getProductOperations( SOURCE, new Date(), new Date(), 1 );

        assertNotNull( pos );
        assertFalse( pos.isEmpty() );
    }


    @Test
    public void testGetProductOperationsException() throws Exception
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        List<ProductOperationView> pos = tracker.getProductOperations( SOURCE, new Date(), new Date(), 1 );

        assertNotNull( pos );
        assertTrue( pos.isEmpty() );
    }


    @Test
    public void testGetProductOperationSources() throws Exception
    {
        List<String> sources = tracker.getProductOperationSources();

        assertNotNull( sources );
        assertFalse( sources.isEmpty() );
    }


    @Test
    public void testGetProductOperationSourcesException() throws Exception
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );

        List<String> sources = tracker.getProductOperationSources();

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

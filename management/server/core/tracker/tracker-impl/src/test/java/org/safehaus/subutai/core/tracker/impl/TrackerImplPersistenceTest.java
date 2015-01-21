/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.util.UUIDUtil;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for TrackerImpl class
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class TrackerImplPersistenceTest
{

    private final UUID poID = UUIDUtil.generateTimeBasedUUID();
    private final String SOURCE = "source";
    private final String DESCRIPTION = "description";

    @Mock
    DataSource dataSource;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    java.sql.ResultSet resultSet;
    private TrackerImpl ti;


    @Before
    public void setupMethod() throws SQLException
    {
        when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );
        when( dataSource.getConnection() ).thenReturn( connection );
        when( preparedStatement.executeQuery() ).thenReturn( resultSet );
        ResultSetMetaData metadata = mock( ResultSetMetaData.class );
        when( metadata.getColumnCount() ).thenReturn( 1 );
        when( metadata.getColumnName( 1 ) ).thenReturn( "info" );
        when( metadata.getColumnType( 1 ) ).thenReturn( java.sql.Types.CLOB );
        when( resultSet.getMetaData() ).thenReturn( metadata );
        when( resultSet.next() ).thenReturn( true ).thenReturn( false );
        //        ti = new TrackerImpl( dataSource );
    }


    @Test
    public void shouldCallDbManagerExecuteUpdateWhenCreatePO() throws SQLException
    {

        ti.createTrackerOperation( SOURCE, DESCRIPTION );

        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void shouldCallDbManagerExecuteUpdateWhenSavePO() throws SQLException
    {

        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, ti );

        ti.saveTrackerOperation( SOURCE, poi );

        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPO() throws SQLException
    {

        ti.getTrackerOperation( SOURCE, poID );

        verify( preparedStatement ).executeUpdate();
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPOs() throws SQLException
    {

        ti.getTrackerOperations( SOURCE, mock( Date.class ), mock( Date.class ), 1 );

        verify( preparedStatement ).executeUpdate();
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPOSources() throws SQLException
    {

        ti.getTrackerOperationSources();

        verify( preparedStatement ).executeUpdate();
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenPrintOperationLog() throws SQLException
    {

        ti.printOperationLog( SOURCE, poID, 100 );

        verify( preparedStatement ).executeUpdate();
    }
}

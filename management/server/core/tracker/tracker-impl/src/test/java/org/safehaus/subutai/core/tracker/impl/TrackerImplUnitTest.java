/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;

import com.datastax.driver.core.ResultSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for TrackerImpl class
 */
public class TrackerImplUnitTest
{

    private final UUID poID = UUID.randomUUID();
    private final String SOURCE = "source";
    private final String DESCRIPTION = "description";

    private DbManager dbManager;
    private TrackerImpl ti;


    @Before
    public void setupMethod() throws DBException
    {
        dbManager = mock( DbManager.class );
        ResultSet rs = mock( ResultSet.class );
        Iterator iterator = mock( Iterator.class );
        when( iterator.hasNext() ).thenReturn( false );
        when( rs.iterator() ).thenReturn( iterator );
        when( dbManager.executeQuery2( any( String.class ), anyVararg() ) ).thenReturn( rs );
        ti = new TrackerImpl();
        ti.setDbManager( dbManager );
    }


    @Test
    public void shouldCallDbManagerExecuteUpdateWhenCreatePO() throws DBException
    {

        ti.createProductOperation( SOURCE, DESCRIPTION );

        verify( dbManager ).executeUpdate2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteUpdateWhenSavePO() throws DBException
    {

        ProductOperationImpl poi = new ProductOperationImpl( SOURCE, DESCRIPTION, ti );

        ti.saveProductOperation( SOURCE, poi );

        verify( dbManager ).executeUpdate2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPO() throws DBException
    {

        ti.getProductOperation( SOURCE, poID );

        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPOs() throws DBException
    {

        ti.getProductOperations( SOURCE, mock( Date.class ), mock( Date.class ), 1 );

        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPOSources() throws DBException
    {

        ti.getProductOperationSources();

        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteQueryWhenPrintOperationLog() throws DBException
    {

        ti.printOperationLog( SOURCE, poID, 100 );

        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.core.tracker.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.util.UUIDUtil;

import io.subutai.core.tracker.impl.TrackerImpl;
import io.subutai.core.tracker.impl.TrackerOperationImpl;
import io.subutai.core.tracker.impl.TrackerOperationViewImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for ProductOperation class
 */
public class TrackerOperationImplTest
{

    private final UUID ID = UUIDUtil.generateTimeBasedUUID();
    private final String SOURCE = "source";
    private final String DUMMY_LOG = "log";
    private final String DESCRIPTION = "description";


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailNullSource()
    {
        new TrackerOperationImpl( null, DESCRIPTION, mock( TrackerImpl.class ) );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailNullDescription()
    {
        new TrackerOperationImpl( SOURCE, null, mock( TrackerImpl.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailNullTracker()
    {
        new TrackerOperationImpl( SOURCE, DESCRIPTION, null );
    }


    @Test
    public void shouldReturnValidValues()
    {
        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );

        assertEquals( DESCRIPTION, poi.getDescription() );
        assertEquals( OperationState.RUNNING, poi.getState() );
        assertNotNull( poi.createDate() );
        assertNotNull( poi.getId() );
    }


    @Test
    public void shouldAddLog()
    {
        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );

        poi.addLog( DUMMY_LOG );
        poi.addLog( DUMMY_LOG );

        assertEquals( DUMMY_LOG + "\n" + DUMMY_LOG, poi.getLog() );
    }


    @Test
    public void shouldAddLogNSucceed()
    {
        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );

        poi.addLogDone( DUMMY_LOG );

        assertEquals( DUMMY_LOG, poi.getLog() );

        assertEquals( OperationState.SUCCEEDED, poi.getState() );
    }


    @Test
    public void shouldAddLogNFail()
    {
        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );

        poi.addLogFailed( DUMMY_LOG );

        assertEquals( DUMMY_LOG, poi.getLog() );

        assertEquals( OperationState.FAILED, poi.getState() );
    }


    @Test
    public void shouldCallTracker()
    {
        TrackerImpl ti = mock( TrackerImpl.class );
        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, ti );

        poi.addLogFailed( DUMMY_LOG );

        verify( ti ).saveTrackerOperation( SOURCE, poi );
    }


    @Test
    public void testHashCodeNEquals() throws Exception
    {

        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );
        Map<TrackerOperationImpl, TrackerOperationImpl> map = new HashMap<>();
        map.put( poi, poi );

        assertEquals( map.get( poi ), poi );
    }


    @Test
    public void testHashCodeNEquals2() throws Exception
    {
        TrackerOperationImpl poi = new TrackerOperationImpl( SOURCE, DESCRIPTION, mock( TrackerImpl.class ) );
        TrackerOperationViewImpl povi = new TrackerOperationViewImpl( poi );
        Map<TrackerOperationViewImpl, TrackerOperationViewImpl> map = new HashMap<>();
        map.put( povi, povi );

        assertEquals( map.get( povi ), povi );
        assertEquals( poi.getId(), povi.getId() );
        assertFalse( povi.equals( null ) );
        assertFalse( povi.equals( poi ) );
    }


    @Test( expected = NullPointerException.class )
    public void poViewConstructorShouldFailNullPO()
    {
        new TrackerOperationViewImpl( null );
    }


    @Test
    public void poViewShouldReturnSameValuesAsPO()
    {
        TrackerOperationImpl poi = mock( TrackerOperationImpl.class );
        when( poi.getId() ).thenReturn( ID );
        when( poi.getDescription() ).thenReturn( DESCRIPTION );
        when( poi.getState() ).thenReturn( OperationState.RUNNING );
        when( poi.getLog() ).thenReturn( DUMMY_LOG );
        when( poi.createDate() ).thenReturn( new Date() );

        TrackerOperationViewImpl povi = new TrackerOperationViewImpl( poi );

        assertEquals( poi.getId(), povi.getId() );
        assertEquals( poi.createDate(), povi.getCreateDate() );
        assertEquals( poi.getLog(), povi.getLog() );
        assertEquals( poi.getDescription(), povi.getDescription() );
        assertEquals( poi.getState(), povi.getState() );
    }
}

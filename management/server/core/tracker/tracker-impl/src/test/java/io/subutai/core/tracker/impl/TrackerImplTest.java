/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.core.tracker.impl;


import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperationView;

import io.subutai.core.tracker.impl.dao.TrackerOperationDataService;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for TrackerImpl class
 */
@RunWith( MockitoJUnitRunner.class )
public class TrackerImplTest extends SystemOutRedirectTest
{
    private static final String SOURCE = "source";
    private static final String DESCRIPTION = "description";
    private static final UUID OPERATION_ID = UUID.randomUUID();

    @Mock
    TrackerOperationDataService dataService;
    @Mock
    private TrackerOperationImpl productOperation;
    @Mock
    private TrackerOperationView productOperationView;
    @Mock
    DaoManager daoManager;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;

    private TrackerImpl tracker;


    @Before
    public void setUp() throws Exception
    {
        tracker = new TrackerImpl();
        tracker.dataService = dataService;
    }


    @Test
    public void testInit() throws Exception
    {
        tracker.setDaoManager( daoManager );
        when( daoManager.getEntityManagerFactory() ).thenReturn( entityManagerFactory );
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );

        tracker.init();

        verify( entityManager ).close();
    }


    @Test
    public void testGetProductOperation() throws Exception
    {
        tracker.getTrackerOperation( SOURCE, OPERATION_ID );

        verify( dataService ).getTrackerOperation( SOURCE, OPERATION_ID );
    }


    @Test
    public void testSaveTrackerOperation() throws Exception
    {
        tracker.saveTrackerOperation( SOURCE, productOperation );

        verify( dataService ).saveTrackerOperation( SOURCE, productOperation );
    }


    @Test
    public void testCreateTrackerOperation() throws Exception
    {
        tracker.createTrackerOperation( SOURCE, DESCRIPTION );
        verify( dataService ).saveTrackerOperation( eq( SOURCE ), isA( TrackerOperationImpl.class ) );
    }


    @Test
    public void testGetTrackerOperations() throws Exception
    {

        tracker.getTrackerOperations( SOURCE, new Date(), new Date(), 1 );

        verify( dataService ).getTrackerOperations( eq( SOURCE ), isA( Date.class ), isA( Date.class ), anyInt() );
    }


    @Test
    public void testGetTrackerOperationSources() throws Exception
    {
        tracker.getTrackerOperationSources();

        verify( dataService ).getTrackerOperationSources();
    }


    @Test
    public void testPrintOperationLog() throws Exception
    {
        when( dataService.getTrackerOperation( SOURCE, OPERATION_ID ) ).thenReturn( productOperationView );
        when( productOperationView.getLog() ).thenReturn( "log" );
        when( productOperationView.getState() ).thenReturn( OperationState.RUNNING )
                                               .thenReturn( OperationState.SUCCEEDED );

        tracker.printOperationLog( SOURCE, OPERATION_ID, 200 );

        assertEquals( "log", getSysOut() );
    }
}

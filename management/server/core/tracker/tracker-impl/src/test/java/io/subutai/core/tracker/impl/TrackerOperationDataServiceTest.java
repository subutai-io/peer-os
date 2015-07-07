package io.subutai.core.tracker.impl;


import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.tracker.impl.TrackerImpl;
import io.subutai.core.tracker.impl.TrackerOperationImpl;
import io.subutai.core.tracker.impl.dao.TrackerOperationDataService;
import io.subutai.core.tracker.impl.entity.TrackerOperationEntity;

import com.google.common.collect.Lists;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TrackerOperationDataServiceTest
{

    private static final String SOURCE = "source";
    private static final String DESCRIPTION = "description";
    private static final UUID ID = UUID.randomUUID();

    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction entityTransaction;
    @Mock
    TypedQuery typedQuery;
    @Mock
    TrackerOperationEntity trackerOperationEntity;
    @Mock
    TrackerImpl tracker;


    TrackerOperationImpl trackerOperation;

    TrackerOperationDataService trackerOperationDataService;


    @Before
    public void setUp() throws Exception
    {
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        trackerOperationDataService = new TrackerOperationDataService( entityManagerFactory );
        when( entityManager.getTransaction() ).thenReturn( entityTransaction );
        when( entityManager.getTransaction().isActive() ).thenReturn( true );
        when( entityManager.createQuery( anyString(), eq( TrackerOperationEntity.class ) ) ).thenReturn( typedQuery );
        when( entityManager.createQuery( anyString(), eq( String.class ) ) ).thenReturn( typedQuery );
        when( entityManager
                .createNamedQuery( TrackerOperationEntity.QUERY_GET_OPERATION, TrackerOperationEntity.class ) )
                .thenReturn( typedQuery );

        trackerOperation = new TrackerOperationImpl( SOURCE, DESCRIPTION, tracker );
    }


    private void throwPersistenceException()
    {
        doThrow( new PersistenceException( "" ) ).when( entityManager )
                                                 .createQuery( anyString(), eq( TrackerOperationEntity.class ) );
        doThrow( new PersistenceException( "" ) ).when( entityManager ).createQuery( anyString(), eq( String.class ) );
        doThrow( new PersistenceException( "" ) ).when( entityManager )
                                                 .createNamedQuery( TrackerOperationEntity.QUERY_GET_OPERATION,
                                                         TrackerOperationEntity.class );
    }


    @Test
    public void testGetAll() throws Exception
    {
        trackerOperationDataService.getAll();

        verify( entityTransaction ).commit();

        throwPersistenceException();

        trackerOperationDataService.getAll();

        verify( entityTransaction ).rollback();
    }


    @Test
    public void testGetTrackerOperation() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( Lists.newArrayList( trackerOperationEntity ) );

        trackerOperationDataService.getTrackerOperation( SOURCE, ID );

        verify( entityTransaction ).commit();

        throwPersistenceException();

        trackerOperationDataService.getTrackerOperation( SOURCE, ID );

        verify( entityTransaction ).rollback();
    }


    @Test( expected = SQLException.class )
    public void testSaveTrackerOperation() throws Exception
    {
        trackerOperationDataService.saveTrackerOperation( SOURCE, trackerOperation );

        verify( entityTransaction ).commit();

        doThrow( new PersistenceException( "" ) ).when( entityTransaction ).begin();

        trackerOperationDataService.saveTrackerOperation( SOURCE, trackerOperation );
    }


    @Test( expected = SQLException.class )
    public void testGetTrackerOperations() throws Exception
    {
        trackerOperationDataService.getTrackerOperations( SOURCE, new Date(), new Date(), 1 );

        verify( entityTransaction ).commit();

        throwPersistenceException();

        trackerOperationDataService.getTrackerOperations( SOURCE, new Date(), new Date(), 1 );

    }


    @Test  ( expected = SQLException.class )
    public void testGetTrackerOperationSources() throws Exception
    {
        trackerOperationDataService.getTrackerOperationSources();

        verify( entityTransaction ).commit();

        throwPersistenceException();

        trackerOperationDataService.getTrackerOperationSources();

    }
}

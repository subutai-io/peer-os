package org.safehaus.subutai.core.tracker.impl.dao;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.impl.TrackerImpl;
import org.safehaus.subutai.core.tracker.impl.TrackerOperationImpl;
import org.safehaus.subutai.core.tracker.impl.TrackerOperationViewImpl;
import org.safehaus.subutai.core.tracker.impl.entity.TrackerOperationEntity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TrackerOperationDataServiceTest
{

    private TrackerOperationDataService dataService;
    private EntityManagerFactory emf;
    private String source = "SOURCE";
    private String description = "Description";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private TrackerOperationImpl to;
    private TrackerOperationEntity toEntity;
    private TrackerImpl tracker;

    private Date fromDate;
    private Date toDate;


    private TrackerOperationViewImpl createTrackerOperation( String infoClob )
    {
        if ( infoClob != null && infoClob.length() > 0 )
        {
            TrackerOperationImpl po = GSON.fromJson( infoClob, TrackerOperationImpl.class );
            return new TrackerOperationViewImpl( po );
        }
        return null;
    }


    @Before
    public void setUp() throws Exception
    {
        emf = Persistence.createEntityManagerFactory( "default" );
        dataService = new TrackerOperationDataService( emf );

        tracker = mock( TrackerImpl.class );

        to = new TrackerOperationImpl( source, description, tracker );
        toEntity = new TrackerOperationEntity( source, to.getId().toString(), to.createDate().getTime(),
                GSON.toJson( to ) );

        fromDate = new Date( new Date().getTime() - 5 * 60 * 1000 );
        toDate = new Date( new Date().getTime() + 5 * 60 * 1000 );
    }


    @After
    public void tearDown() throws Exception
    {
        emf.close();
    }


    @Test
    public void testGetAll() throws Exception
    {
        dataService.saveTrackerOperation( source, to );
        List<TrackerOperationEntity> operations = dataService.getAll();
        assertTrue( operations.contains( toEntity ) );
    }


    @Test
    public void testGetAllShouldRollbackTransaction() throws SQLException
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        when( transaction.isActive() ).thenReturn( true );
        when( em.getTransaction() ).thenThrow( PersistenceException.class ).thenReturn( transaction, transaction );
        when( emf.createEntityManager() ).thenReturn( em ).thenReturn( em );

        dataService.saveTrackerOperation( source, to );
        dataService = new TrackerOperationDataService( emf );
        List<TrackerOperationEntity> operations = dataService.getAll();
    }


    @Test
    public void testGetTrackerOperation() throws Exception
    {
        dataService.saveTrackerOperation( source, to );
        TrackerOperationView result = dataService.getTrackerOperation( source, to.getId() );
        TrackerOperationView savedResult = createTrackerOperation( toEntity.getInfo() );

        assertEquals( savedResult, result );
    }


    @Test
    public void testGetTrackerOperationShouldReturnNullAndRollback() throws Exception
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        when( transaction.isActive() ).thenReturn( true );
        when( em.getTransaction() ).thenThrow( PersistenceException.class ).thenReturn( transaction, transaction );
        when( emf.createEntityManager() ).thenReturn( em ).thenReturn( em );

        dataService.saveTrackerOperation( source, to );
        dataService = new TrackerOperationDataService( emf );
        TrackerOperationView result = dataService.getTrackerOperation( source, to.getId() );
        assertNull( result );
    }


    @Test
    public void testGetTrackerOperationShouldReturnNull() throws Exception
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        TypedQuery<TrackerOperationEntity> query = mock( TypedQuery.class );
        TrackerOperationEntity operationEntity = mock( TrackerOperationEntity.class );

        List<TrackerOperationEntity> operationList = new ArrayList<>();
        operationList.add( operationEntity );

        when( operationEntity.getInfo() ).thenReturn( "" );
        when( query.getResultList() ).thenReturn( operationList );
        when( query.setParameter( anyString(), anyObject() ) ).thenReturn( query, query );
        when( transaction.isActive() ).thenReturn( true );
        when( em.getTransaction() ).thenReturn( transaction, transaction );
        when( em.createNamedQuery( TrackerOperationEntity.QUERY_GET_OPERATION, TrackerOperationEntity.class ) )
                .thenReturn( query );
        when( emf.createEntityManager() ).thenReturn( em );

        dataService = new TrackerOperationDataService( emf );
        TrackerOperationView result = dataService.getTrackerOperation( source, to.getId() );
        assertNull( result );
    }


    @Test
    public void testSaveTrackerOperation() throws Exception
    {
        dataService.saveTrackerOperation( source, to );
        List<TrackerOperationEntity> operations = dataService.getAll();
        assertTrue( operations.contains( toEntity ) );
    }


    @Test( expected = SQLException.class )
    public void testSaveTrackerOperationShouldFailToSave() throws Exception
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        when( transaction.isActive() ).thenReturn( true );
        when( em.getTransaction() ).thenThrow( PersistenceException.class ).thenReturn( transaction, transaction );
        when( emf.createEntityManager() ).thenReturn( em ).thenReturn( em );

        dataService = new TrackerOperationDataService( emf );
        dataService.saveTrackerOperation( source, to );
    }


    @Test
    public void testGetTrackerOperations() throws Exception
    {
        dataService.saveTrackerOperation( source, to );
        List<TrackerOperationView> operationViewList = dataService.getTrackerOperations( source, fromDate, toDate, 50 );
        TrackerOperationView savedResult = createTrackerOperation( toEntity.getInfo() );

        assertTrue( operationViewList.contains( savedResult ) );
    }


    @Test( expected = SQLException.class )
    public void testGetTrackerOperationsShouldRollbackTransaction() throws Exception
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        when( transaction.isActive() ).thenReturn( true );
        when( em.getTransaction() ).thenThrow( PersistenceException.class ).thenReturn( transaction, transaction );
        when( emf.createEntityManager() ).thenReturn( em ).thenReturn( em );

        dataService.saveTrackerOperation( source, to );
        dataService = new TrackerOperationDataService( emf );
        List<TrackerOperationView> operationViewList = dataService.getTrackerOperations( source, fromDate, toDate, 50 );
    }


    @Test
    public void testGetTrackerOperationSources() throws Exception
    {
        List<String> sources = dataService.getTrackerOperationSources();
        assertTrue( sources.contains( source ) );
    }


    @Test( expected = SQLException.class )
    public void testGetTrackerOperationSourcesShouldRollbackOperation() throws Exception
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        when( transaction.isActive() ).thenReturn( true );
        when( em.getTransaction() ).thenThrow( PersistenceException.class ).thenReturn( transaction, transaction );
        when( emf.createEntityManager() ).thenReturn( em ).thenReturn( em );

        dataService = new TrackerOperationDataService( emf );
        List<String> sources = dataService.getTrackerOperationSources();
    }
}
package io.subutai.core.identity.impl.dao;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.impl.dao.RestEndpointDataService;
import io.subutai.core.identity.impl.entity.RestEndpointScopeEntity;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RestEndpointDataServiceTest
{
    private RestEndpointDataService restEndpointDataService;
    private List<RestEndpointScopeEntity> myList;
    private List<Object> list;

    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    Query query;
    @Mock
    RestEndpointScopeEntity restEndpointScopeEntity;
    @Mock
    TypedQuery<Object> typedQuery;
    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        restEndpointDataService = new RestEndpointDataService( entityManagerFactory );
        restEndpointDataService.setEmf( entityManagerFactory );

        myList = new ArrayList<>();
        myList.add( restEndpointScopeEntity );
        list = Lists.newArrayList();
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.createQuery( anyString(), Matchers.<Class<Object>>anyObject() ) ).thenReturn( typedQuery );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( entityManager.getTransaction() ).thenReturn( transaction );
    }


    @Test
    public void testGetAll() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( list );

        restEndpointDataService.getAll();

        verify( transaction ).begin();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        restEndpointDataService.getAll();

        verify( transaction ).rollback();
    }


    @Test
    public void testFind() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( list );

        restEndpointDataService.find( "55" );
    }


    @Test
    public void testFindException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        restEndpointDataService.find( "55" );

        verify( transaction ).rollback();
    }


    @Test
    public void testPersist() throws Exception
    {
        restEndpointDataService.persist( restEndpointScopeEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test( expected = Exception.class )
    public void testPersistException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        restEndpointDataService.persist( restEndpointScopeEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testRemove() throws Exception
    {
        restEndpointDataService.remove( "55" );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testRemoveException() throws Exception
    {
        when( query.executeUpdate() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );
        restEndpointDataService.remove( "55" );
    }


    @Test
    public void testUpdate() throws Exception
    {
        restEndpointDataService.update( restEndpointScopeEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test( expected = Exception.class )
    public void testUpdateException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        restEndpointDataService.update( restEndpointScopeEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testGetEmf() throws Exception
    {
        assertNotNull( restEndpointDataService.getEmf() );
    }
}
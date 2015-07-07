package io.subutai.core.identity.impl.dao;


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

import io.subutai.core.identity.impl.dao.PortalModuleDataService;
import io.subutai.core.identity.impl.entity.PortalModuleScopeEntity;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PortalModuleDataServiceTest
{
    private PortalModuleDataService portalModuleDataService;
    private List<Object> myList;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    Query query;
    @Mock
    PortalModuleScopeEntity portalModuleScopeEntity;
    @Mock
    TypedQuery<Object> typedQuery;
    @Mock
    Object object;

    @Before
    public void setUp() throws Exception
    {
        portalModuleDataService = new PortalModuleDataService( entityManagerFactory );
        portalModuleDataService.setEmf( entityManagerFactory );

        myList = Lists.newArrayList();

        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( entityManager.createQuery( anyString() ) ).thenReturn(query  );
        when( entityManager.createQuery( anyString(), Matchers.<Class<Object>>anyObject() ) ).thenReturn( typedQuery );

    }


    @Test
    public void testGetAll() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( myList );

        portalModuleDataService.getAll();

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        portalModuleDataService.getAll();

        verify( transaction ).rollback();
    }



    @Test
    public void testFind() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( myList );

        portalModuleDataService.find( "55" );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testFindException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        portalModuleDataService.find( "id" );

        verify( transaction ).rollback();
    }


    @Test
    public void testPersist() throws Exception
    {
        portalModuleDataService.persist( portalModuleScopeEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager).close();
    }


    @Test(expected = Exception.class)
    public void testPersistException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        portalModuleDataService.persist( portalModuleScopeEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testRemove() throws Exception
    {
        portalModuleDataService.remove( "55" );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager).close();
    }


    @Test
    public void testRemoveException() throws Exception
    {
        when( query.executeUpdate() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );
        portalModuleDataService.remove( "55" );
    }


    @Test
    public void testUpdate() throws Exception
    {
        portalModuleDataService.update( portalModuleScopeEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager).close();
    }

    @Test(expected = Exception.class)
    public void testUpdateException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        portalModuleDataService.update( portalModuleScopeEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testGetEmf() throws Exception
    {
        assertNotNull(portalModuleDataService.getEmf());
    }

}
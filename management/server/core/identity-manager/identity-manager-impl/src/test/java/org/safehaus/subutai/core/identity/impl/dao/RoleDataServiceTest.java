package org.safehaus.subutai.core.identity.impl.dao;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.impl.entity.RoleEntity;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RoleDataServiceTest
{
    private RoleDataService roleDataService;
    private List<Role> myList;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    Query query;
    @Mock
    Role role;
    @Mock
    RoleEntity roleEntity;

    @Before
    public void setUp() throws Exception
    {
        roleDataService = new RoleDataService( entityManagerFactory );
        roleDataService.setEntityManagerFactory( entityManagerFactory );

        myList = new ArrayList<>(  );
        myList.add( role );

        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( transaction.isActive() ).thenReturn( true );
    }


    @Test
    public void testFind() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenReturn( roleEntity );

        roleDataService.find( "test" );
    }


    @Test
    public void testFindException() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenThrow( Exception.class );

        roleDataService.find( "test" );

        verify( transaction ).rollback();
    }


    @Test
    public void testGetAll() throws Exception
    {
        when( query.getResultList() ).thenReturn( myList );

        roleDataService.getAll();

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllException() throws Exception
    {
        when( query.getResultList() ).thenThrow( Exception.class );

        roleDataService.getAll();

        verify( transaction ).rollback();
    }


    @Test
    public void testPersist() throws Exception
    {
        roleDataService.persist( role );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test(expected = Exception.class)
    public void testPersistException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        roleDataService.persist( role );
    }



    @Test
    public void testRemove() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenReturn( roleEntity );

        roleDataService.remove( "id" );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testRemoveException() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenThrow( Exception.class );

        roleDataService.remove( "id" );

        verify( transaction ).rollback();
    }


    @Test
    public void testUpdate() throws Exception
    {
        roleDataService.update( role );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test(expected = Exception.class)
    public void testUpdateException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        roleDataService.update( role );
    }

}
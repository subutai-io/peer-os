package io.subutai.core.identity.impl.dao;


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
import io.subutai.core.identity.api.PermissionGroup;
import io.subutai.core.identity.impl.dao.PermissionDataService;
import io.subutai.core.identity.impl.entity.PermissionEntity;
import io.subutai.core.identity.impl.entity.PermissionPK;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PermissionDataServiceTest
{
    private PermissionDataService permissionDataService;

    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    Query query;
    @Mock
    PermissionPK permissionPK;
    @Mock
    PermissionEntity permissionEntity;
    @Mock
    TypedQuery<Object> typedQuery;

    @Before
    public void setUp() throws Exception
    {
        permissionDataService = new PermissionDataService( entityManagerFactory );
        permissionDataService.setEmf( entityManagerFactory );

        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( entityManager.createQuery( anyString() ) ).thenReturn(query  );
        when( entityManager.createQuery( anyString(), Matchers.<Class<Object>>anyObject() ) ).thenReturn( typedQuery );
    }


    @Test(expected = Exception.class)
    public void testGetAllException() throws Exception
    {
        when( transaction.isActive() ).thenReturn( true );
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        permissionDataService.getAll();

        verify( transaction ).begin();
        verify( entityManager ).close();
    }

    @Test
    public void testGetAll()
    {
        permissionDataService.getAll();

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllByPermissionGroup() throws Exception
    {
        permissionDataService.getAllByPermissionGroup( PermissionGroup.PEER_PERMISSIONS );

        verify( transaction ).begin();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllByPermissionGroupException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        permissionDataService.getAllByPermissionGroup( PermissionGroup.PEER_PERMISSIONS );

        verify( transaction ).rollback();
    }


    @Test
    public void testFind() throws Exception
    {
        permissionDataService.find( permissionPK );

        verify( transaction ).begin();
        verify( entityManager ).close();
    }


    @Test
    public void testFindException() throws Exception
    {
        when( transaction.isActive() ).thenReturn( true );
        when( typedQuery.getResultList() ).thenThrow( Exception.class );

        permissionDataService.find( permissionPK );

        verify( transaction ).rollback();
    }



    @Test
    public void testPersist() throws Exception
    {
        permissionDataService.persist( permissionEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager).close();
    }


    @Test(expected = Exception.class)
    public void testPersistException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        permissionDataService.persist( permissionEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testRemove() throws Exception
    {
        when( permissionPK.getPermissionKey() ).thenReturn( "permissionKey" );
        when( permissionPK.getPermissionGroup() ).thenReturn( PermissionGroup.DEFAULT_PERMISSIONS );

        permissionDataService.remove( permissionPK );
    }

    @Test
    public void testRemoveException() throws Exception
    {
        when( permissionPK.getPermissionKey() ).thenReturn( "permissionKey" );
        when( permissionPK.getPermissionGroup() ).thenReturn( PermissionGroup.DEFAULT_PERMISSIONS );
        when( query.executeUpdate() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        permissionDataService.remove( permissionPK );
    }



    @Test
    public void testUpdate() throws Exception
    {
        permissionDataService.update( permissionEntity );
    }


    @Test(expected = Exception.class)
    public void testUpdateException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        permissionDataService.update( permissionEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testGetEmf() throws Exception
    {
        assertNotNull( permissionDataService.getEmf());
    }
}
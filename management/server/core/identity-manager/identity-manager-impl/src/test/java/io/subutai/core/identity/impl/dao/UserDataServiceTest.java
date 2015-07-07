package io.subutai.core.identity.impl.dao;


import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.User;
import io.subutai.core.identity.impl.entity.UserEntity;

import com.google.common.collect.Lists;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class UserDataServiceTest
{
    private UserDataService userDataService;
    private Collection<User> myList;

    @Mock
    DaoManager daoManager;
    @Mock
    User user;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    UserEntity userEntity;
    @Mock
    Query query;
    @Mock
    TypedQuery<Object> typedQuery;


    @Before
    public void setUp() throws Exception
    {
        userDataService = new UserDataService( daoManager );

        myList = Lists.newArrayList();
        myList.add( user );
        when( daoManager.getEntityManagerFromFactory() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( entityManager.createQuery( anyString() , Matchers.<Class<Object>>anyObject() )).thenReturn( typedQuery );
        when( transaction.isActive() ).thenReturn( true );

    }


    @Test
    public void testFind() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenReturn( userEntity );

        userDataService.find( ( long ) 5 );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }

    @Test
    public void testFindException() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenThrow( Exception.class );

        userDataService.find( ( long ) 5 );

        verify( transaction ).rollback();
    }



    @Test
    public void testGetAll() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( ( List ) myList );

        userDataService.getAll();

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );

        userDataService.getAll();

        verify( transaction ).rollback();
    }


    @Test
    public void testPersist() throws Exception
    {
        userDataService.persist( user );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test(expected = Exception.class)
    public void testPersistException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        userDataService.persist( user );
    }


    @Test
    public void testRemove() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenReturn( userEntity );

        userDataService.remove( ( long ) 5 );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }

    @Test
    public void testRemoveException() throws Exception
    {
        when( entityManager.find( Matchers.<Class<Object>>anyObject(), anyString() ) ).thenThrow( Exception.class );

        userDataService.remove( ( long ) 5 );

        verify( transaction ).rollback();
    }



    @Test
    public void testUpdate() throws Exception
    {
        userDataService.update( user );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test(expected = Exception.class)
    public void testUpdateException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        userDataService.update( user );
    }


    @Test
    public void testFindByUsername() throws Exception
    {
        when( typedQuery.getResultList() ).thenReturn( ( List ) myList );
        when( transaction.isActive() ).thenReturn( true );

        userDataService.findByUsername( "userName" );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testFindByUsernameException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );

        when( transaction.isActive() ).thenReturn( true );

        userDataService.findByUsername( "userName" );

        verify( transaction ).rollback();
    }
}
package io.subutai.core.channel.impl.token;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.channel.impl.entity.UserChannelToken;
import io.subutai.core.channel.impl.token.ChannelTokenManagerImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ChannelTokenManagerImplTest
{
    private ChannelTokenManagerImpl channelTokenManager;

    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    Query query;
    @Mock
    Object object;
    @Mock
    UserChannelToken userChannelToken;
    @Mock
    EntityTransaction entityTransaction;


    @Before
    public void setUp() throws Exception
    {
        channelTokenManager = new ChannelTokenManagerImpl();
        channelTokenManager.setEntityManagerFactory( entityManagerFactory );

        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( entityManager.getTransaction() ).thenReturn( entityTransaction );
    }


    @Test
    public void testGetEntityManagerFactory() throws Exception
    {
        assertNotNull( channelTokenManager.getEntityManagerFactory() );
    }


    @Test
    public void testGetUserChannelTokenIdUserNull() throws Exception
    {
        when( query.getSingleResult() ).thenReturn( null );

        channelTokenManager.getUserChannelTokenId( "token" );

        assertEquals( 0, channelTokenManager.getUserChannelTokenId( "token" ) );
    }


    @Test
    public void testGetUserChannelTokenId() throws Exception
    {
        when( query.getSingleResult() ).thenReturn( userChannelToken );
        when( userChannelToken.getUserId() ).thenReturn( ( long ) 5 );

        channelTokenManager.getUserChannelTokenId( "token" );

        assertEquals( 5, channelTokenManager.getUserChannelTokenId( "token" ) );
    }


    @Test
    public void testGetUserChannelTokenIdException() throws Exception
    {
        when( query.getSingleResult() ).thenThrow( Exception.class );
        when( entityManager.isOpen() ).thenReturn( true );

        channelTokenManager.getUserChannelTokenId( "token" );

        verify( entityManager ).close();
    }


    @Test
    public void testGetUserChannelToken() throws Exception
    {
        when( query.getSingleResult() ).thenReturn( userChannelToken );

        channelTokenManager.getUserChannelToken( "token" );
    }


    @Test
    public void testGetUserChannelTokenException() throws Exception
    {
        when( query.getSingleResult() ).thenThrow( Exception.class );
        when( entityManager.isOpen() ).thenReturn( true );

        channelTokenManager.getUserChannelToken( "token" );

        verify( entityManager ).close();
    }


    @Test
    public void testSetTokenValidityException() throws Exception
    {
        when( entityTransaction.isActive() ).thenReturn( true );
        when( entityManager.isOpen() ).thenReturn( true );

        channelTokenManager.setTokenValidity();
    }


    @Test
    public void testSetTokenValidity() throws Exception
    {
        when( entityManager.createNativeQuery( anyString() ) ).thenReturn( query );
        when( query.executeUpdate() ).thenReturn( 1 );

        channelTokenManager.setTokenValidity();

        verify( query ).executeUpdate();
        verify( entityTransaction ).commit();
    }


    @Test
    public void testSaveUserChannelToken() throws Exception
    {
        channelTokenManager.saveUserChannelToken( userChannelToken );

        verify( entityTransaction ).commit();
    }


    @Test( expected = Exception.class )
    public void testSaveUserChannelTokenException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );
        when( entityManager.isOpen() ).thenReturn( true );

        channelTokenManager.saveUserChannelToken( userChannelToken );
    }


    @Test
    public void testRemoveUserChannelToken() throws Exception
    {
        channelTokenManager.removeUserChannelToken( "token" );

        verify( entityTransaction ).commit();
    }


    @Test
    public void testRemoveUserChannelTokenException() throws Exception
    {
        when( entityTransaction.isActive() ).thenReturn( true );
        when( entityManager.isOpen() ).thenReturn( true );

        when( query.executeUpdate() ).thenThrow( Exception.class );

        channelTokenManager.removeUserChannelToken( "token" );
    }


    @Test
    public void testGetUserChannelTokenData() throws Exception
    {
        channelTokenManager.getUserChannelTokenData( 5 );

        verify( query ).getResultList();
    }


    @Test
    public void testGetUserChannelTokenDataException() throws Exception
    {
        when( query.getResultList() ).thenThrow( Exception.class );
        when( entityManager.isOpen() ).thenReturn( true );

        channelTokenManager.getUserChannelTokenData( 5 );
    }


    @Test
    public void testGetAllUserChannelTokenData() throws Exception
    {
        channelTokenManager.getAllUserChannelTokenData();

        verify( query ).getResultList();
    }


    @Test
    public void testGetAllUserChannelTokenDataException() throws Exception
    {
        when( query.getResultList() ).thenThrow( Exception.class );
        when( entityManager.isOpen() ).thenReturn( true );

        channelTokenManager.getAllUserChannelTokenData();
    }


    @Test
    public void testCreateUserChannelToken() throws Exception
    {
        assertNotNull( channelTokenManager.createUserChannelToken() );
    }
}
package io.subutai.core.peer.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.util.JsonUtil;

import io.subutai.core.peer.impl.entity.PeerData;

import com.google.common.collect.Lists;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PeerDAOTest
{
    private static final String SOURCE = "source";
    private static final String KEY = "key";
    @Mock
    RuntimeException exception;
    @Mock
    DaoManager daoManager;
    @Mock
    EntityManager entityManager;
    @Mock
    Query query;
    @Mock
    TypedQuery<PeerData> typedQuery;
    @Mock
    PeerData peerData;

    Object info;


    PeerDAO peerDAO;


    @Before
    public void setUp() throws Exception
    {
        peerDAO = new PeerDAO( daoManager );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( entityManager.createQuery( anyString(), eq( PeerData.class ) ) ).thenReturn( typedQuery );
        when( daoManager.getEntityManagerFromFactory() ).thenReturn( entityManager );
        info = new Object();
        when( typedQuery.getResultList() ).thenReturn( Lists.newArrayList( peerData ) );
        when( typedQuery.getSingleResult() ).thenReturn( peerData );
        when( peerData.getInfo() ).thenReturn( JsonUtil.toJson( info ) );
    }


    private void verifyCommitNClose()
    {
        verify( daoManager ).commitTransaction( entityManager );
        verify( daoManager ).closeEntityManager( entityManager );
    }


    private void verifyClose()
    {
        verify( daoManager ).closeEntityManager( entityManager );
    }


    private void verifyRollbackNClose()
    {
        verify( daoManager ).rollBackTransaction( entityManager );
        verify( daoManager, times( 2 ) ).closeEntityManager( entityManager );
    }


    private void throwException()
    {
        doThrow( exception ).when( daoManager ).startTransaction( entityManager );

        doThrow( exception ).when( entityManager ).createQuery( anyString(), eq( PeerData.class ) );
    }


//    @Test
//    public void testSaveInfo() throws Exception
//    {
//        peerDAO.saveInfo( SOURCE, KEY, info );
//
//        verifyCommitNClose();
//
//        throwException();
//
//        peerDAO.saveInfo( SOURCE, KEY, info );
//
//        verifyRollbackNClose();
//    }


    @Test
    public void testDeleteInfo() throws Exception
    {
        peerDAO.deleteInfo( SOURCE, KEY );

        verifyCommitNClose();

        throwException();

        peerDAO.deleteInfo( SOURCE, KEY );

        verifyRollbackNClose();
    }


    @Test
    public void testGetInfo() throws Exception
    {

        List<Object> infos = peerDAO.getInfo( SOURCE, Object.class );

        verifyClose();

        assertFalse( infos.isEmpty() );

        throwException();

        peerDAO.getInfo( SOURCE, Object.class );

        verify( exception ).getMessage();
    }


    @Test
    public void testGetInfo2() throws Exception
    {
        Object info = peerDAO.getInfo( SOURCE, KEY, Object.class );

        verifyClose();

        assertNotNull( info );

        throwException();

        peerDAO.getInfo( SOURCE, KEY, Object.class );

        verify( exception ).getMessage();
    }
}

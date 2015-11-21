package io.subutai.core.identity.impl.dao;


import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.impl.model.SessionEntity;


/**
 *
 */
class SessionDAO
{
    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    public SessionDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public Session find( final long id )
    {
        Session result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( SessionEntity.class, id );
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    public List<Session> getByUserId( final long userId )
    {
        List<Session> results = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            Query qr = em.createQuery( "select se from SessionEntity se where se.user.id=:userId" );
            qr.setParameter( "userId", userId );
            results = qr.getResultList();
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return results;
    }


    /* *************************************************
     *
     */
    public Session getValid( final long userId )
    {
        Session result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            Query qr = em.createQuery( "select se from SessionEntity se where se.status=1 and se.user.id=:userId" );
            qr.setParameter( "userId", userId );
            result = (Session)qr.getResultList().get( 0 );
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    public void invalidate()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            Query qr = em.createQuery( "update SessionEntity as se SET es.status=0 where se.startDate<:CurrentDate" );
            qr.setParameter( "CurrentDate", new Date( System.currentTimeMillis() ) );
            qr.executeUpdate();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public List<Session> getAll()
    {
        List<Session> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.createQuery( "select h from SessionEntity h" ).getResultList();
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    public void persist( final Session item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.persist( item );
            em.flush();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public void remove( final long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            Session item = em.find( SessionEntity.class, id );
            em.remove( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public void update( final Session item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.merge( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}

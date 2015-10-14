package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.PermissionObject;
import io.subutai.core.identity.impl.model.PermissionObjectEntity;


/**
 *
 */
class PermissionObjectDAO
{
    private DaoManager daoManager= null;


    /* *************************************************
     *
     */
    public PermissionObjectDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }

    /* *************************************************
     *
     */
    public PermissionObject find( final long id )
    {
        PermissionObject result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( PermissionObjectEntity.class, id );
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
    public List<PermissionObject> getAll()
    {
        List<PermissionObject> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.createQuery( "select h from PermissionObjectEntity h" ).getResultList();
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
    public void persist( final PermissionObject item )
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
            PermissionObject item = em.find( PermissionObjectEntity.class, id );
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
    public void update( final PermissionObject item )
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

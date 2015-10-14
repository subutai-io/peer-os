package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.PermissionOperation;
import io.subutai.core.identity.impl.model.PermissionOperationEntity;


/**
 *
 */
class PermissionOperationDAO
{
    private DaoManager daoManager= null;


    /* *************************************************
     *
     */
    public PermissionOperationDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public PermissionOperation find( final long id )
    {
        PermissionOperation result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( PermissionOperationEntity.class, id );
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
    public List<PermissionOperation> getAll()
    {
        List<PermissionOperation> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.createQuery( "select h from PermissionOperationEntity h" ).getResultList();
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
    public void persist( final PermissionOperation item )
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
            PermissionOperation item = em.find( PermissionOperationEntity.class, id );
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
    public void update( final PermissionOperation item )
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
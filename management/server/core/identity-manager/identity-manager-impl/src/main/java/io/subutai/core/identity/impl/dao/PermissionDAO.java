package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.impl.model.PermissionEntity;


/**
 *
 */
class PermissionDAO
{
    private DaoManager daoManager= null;


    /* *************************************************
     *
     */
    public PermissionDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public Permission find( final long id )
    {
        Permission result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( PermissionEntity.class, id );
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
    public List<Permission> getAll()
    {
        List<Permission> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.createQuery( "select h from PermissionEntity h" ).getResultList();
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
    public void persist( final Permission item )
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
            PermissionEntity item = em.find( PermissionEntity.class, id );
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
    public void update( final Permission item )
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

package io.subutai.core.identity.impl.dao;


import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.RolePermission;
import io.subutai.core.identity.impl.model.RolePermissionEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.List;


public class RolePermissionDAO
{
    private DaoManager daoManager = null;


    public RolePermissionDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public RolePermission persist( final Long roleId, final Permission perm )
    {
        RolePermission item = new RolePermissionEntity();
        item.setRoleId( roleId );
        item.setPermissionId( perm.getId() );
        item.setScope( perm.getScope() );
        item.setRead( perm.isRead() );
        item.setWrite( perm.isWrite() );
        item.setUpdate( perm.isUpdate() );
        item.setDelete( perm.isDelete() );
        item.setObjectName( perm.getObjectName() );
        item.setPermObject( perm.getObject() );

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
            e.printStackTrace();
            daoManager.rollBackTransaction( em );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
            return item;
        }
    }


    public void remove( final RolePermission rp )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            RolePermissionEntity item = em.find( RolePermissionEntity.class, rp.getId() );
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


    public void update( final RolePermission item )
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


    public List<RolePermission> getAll( final Long roleId )
    {
        List<RolePermission> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        Query query = null;
        try
        {
            query = em.createQuery( "select h from RolePermissionEntity h where h.roleId = :roleId " );
            query.setParameter( "roleId", roleId );
            result = ( List<RolePermission> ) query.getResultList();
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
}

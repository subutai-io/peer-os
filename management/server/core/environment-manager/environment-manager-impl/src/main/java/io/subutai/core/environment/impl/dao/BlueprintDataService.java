package io.subutai.core.environment.impl.dao;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentBlueprintEntity;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class BlueprintDataService
{

    private static final Logger LOG = LoggerFactory.getLogger( BlueprintDataService.class.getName() );

    private DaoManager daoManager;


    public BlueprintDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /**
     * Save blueprint to database
     *
     * @param blueprint - object to save
     */
    public void persist( final Blueprint blueprint ) throws EnvironmentManagerException
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            EnvironmentBlueprintEntity environmentBlueprintEntity = new EnvironmentBlueprintEntity();
            environmentBlueprintEntity.setId( blueprint.getId().toString() );
            environmentBlueprintEntity.setInfo( JsonUtil.toJson( blueprint ) );

            daoManager.startTransaction( em );
            em.merge( environmentBlueprintEntity );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
            throw new EnvironmentManagerException( "Failed to save blueprint", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /**
     * Returns list of blueprints saved in database
     *
     * @return - Set of {@link io.subutai.common.environment.Blueprint}
     */
    public Set<Blueprint> getAll()
    {
        Set<Blueprint> blueprints = Sets.newHashSet();

        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            TypedQuery<EnvironmentBlueprintEntity> query =
                    em.createQuery( "SELECT ebe FROM EnvironmentBlueprintEntity AS ebe",
                            EnvironmentBlueprintEntity.class );

            List<EnvironmentBlueprintEntity> results = query.getResultList();

            for ( EnvironmentBlueprintEntity ebe : results )
            {
                Blueprint blueprint = JsonUtil.fromJson( ebe.getInfo(), Blueprint.class );
                blueprints.add( blueprint );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to get blueprints", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return blueprints;
    }


    /**
     * Delete {@link io.subutai.common.environment.Blueprint} from database by id
     *
     * @param id - blueprint id to remove
     */
    public void remove( final UUID id ) throws EnvironmentManagerException
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( em );

            Query query;
            query = em.createQuery( "delete FROM EnvironmentBlueprintEntity AS ebe WHERE ebe.id=:id" );
            query.setParameter( "id", id.toString() );
            query.executeUpdate();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            throw new EnvironmentManagerException( "Failed to remove blueprint", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public Blueprint find( final UUID id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            EnvironmentBlueprintEntity r = em.find( EnvironmentBlueprintEntity.class, id.toString() );
            return JsonUtil.fromJson( r.getInfo(), Blueprint.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to find blueprint by id {}", id, e );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}


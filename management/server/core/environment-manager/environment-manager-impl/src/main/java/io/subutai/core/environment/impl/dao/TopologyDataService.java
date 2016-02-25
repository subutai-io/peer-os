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
import io.subutai.common.environment.Topology;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentTopologyEntity;


public class TopologyDataService
{

    private static final Logger LOG = LoggerFactory.getLogger( TopologyDataService.class );

    private DaoManager daoManager;


    public TopologyDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /**
     * Save topology to database
     *
     * @param topology - object to save
     */
    public void persist( final Topology topology ) throws EnvironmentManagerException
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            EnvironmentTopologyEntity environmentTopologyEntity = new EnvironmentTopologyEntity();
            environmentTopologyEntity.setId( topology.getId().toString() );
            environmentTopologyEntity.setInfo( JsonUtil.toJson( topology ) );

            daoManager.startTransaction( em );
            em.merge( environmentTopologyEntity );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
            throw new EnvironmentManagerException( "Failed to save topology", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /**
     * Returns list of topologies saved in database
     *
     * @return - Set of {@link io.subutai.common.environment.Topology}
     */
    public Set<Topology> getAll()
    {
        Set<Topology> topologies = Sets.newHashSet();

        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            TypedQuery<EnvironmentTopologyEntity> query =
                    em.createQuery( "SELECT ebe FROM EnvironmentTopologyEntity AS ebe",
                            EnvironmentTopologyEntity.class );

            List<EnvironmentTopologyEntity> results = query.getResultList();

            for ( EnvironmentTopologyEntity ebe : results )
            {
                Topology blueprint = JsonUtil.fromJson( ebe.getInfo(), Topology.class );
                topologies.add( blueprint );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to get topologies", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return topologies;
    }


    /**
     * Delete {@link io.subutai.common.environment.Topology} from database by id
     *
     * @param id - topology id to remove
     */
    public void remove( final UUID id ) throws EnvironmentManagerException
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( em );

            Query query;
            query = em.createQuery( "delete FROM EnvironmentTopologyEntity AS ebe WHERE ebe.id=:id" );
            query.setParameter( "id", id.toString() );
            query.executeUpdate();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            throw new EnvironmentManagerException( "Failed to remove topology", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public Topology find( final UUID id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            EnvironmentTopologyEntity r = em.find( EnvironmentTopologyEntity.class, id.toString() );
            return JsonUtil.fromJson( r.getInfo(), Topology.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to find topology by id {}", id, e );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}


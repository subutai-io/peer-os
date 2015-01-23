package org.safehaus.subutai.core.env.impl.dao;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentBlueprintEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class BlueprintDataService
{

    private static final Logger LOG = LoggerFactory.getLogger( BlueprintDataService.class.getName() );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private DaoManager daoManager;


    public BlueprintDataService( DaoManager daoManager ) throws SQLException
    {
        this.daoManager = daoManager;
    }


    public UUID persist( final EnvironmentBlueprint blueprint )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        UUID bpId = null;

        try
        {
            String json = GSON.toJson( blueprint );

            EnvironmentBlueprintEntity environmentBlueprintEntity = new EnvironmentBlueprintEntity();
            environmentBlueprintEntity.setId( blueprint.getId().toString() );
            environmentBlueprintEntity.setInfo( json );

            daoManager.startTransaction( em );
            em.merge( environmentBlueprintEntity );
            daoManager.commitTransaction( em );

            bpId = blueprint.getId();
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to save blueprint", e );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return bpId;
    }


    public List<EnvironmentBlueprint> getAll()
    {
        List<EnvironmentBlueprint> blueprints = new ArrayList<>();
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            TypedQuery<EnvironmentBlueprintEntity> query =
                    em.createQuery( "SELECT ebe FROM EnvironmentBlueprintEntity AS ebe",
                            EnvironmentBlueprintEntity.class );

            List<EnvironmentBlueprintEntity> results = query.getResultList();

            for ( EnvironmentBlueprintEntity ebe : results )
            {
                blueprints.add( GSON.fromJson( ebe.getInfo(), EnvironmentBlueprint.class ) );
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


    public void remove( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( em );

            Query query;
            query = em.createQuery( "delete FROM EnvironmentBlueprintEntity AS ebe WHERE ebe.id=:id" );
            query.setParameter( "id", id );
            query.executeUpdate();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to delete blueprint", e );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public EnvironmentBlueprint find( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            EnvironmentBlueprintEntity ebe = em.find( EnvironmentBlueprintEntity.class, id );

            if ( ebe != null )
            {
                return GSON.fromJson( ebe.getInfo(), EnvironmentBlueprint.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return null;
    }
}


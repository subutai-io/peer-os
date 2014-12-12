package org.safehaus.subutai.plugin.common.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.plugin.common.model.ClusterDataEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by talas on 12/9/14.
 */
public class PluginDataService
{
    private EntityManagerFactory emf;
    private Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LoggerFactory.getLogger( PluginDataService.class );


    public PluginDataService( final EntityManagerFactory emf ) throws SQLException
    {
        this.emf = emf;
        try
        {
            this.emf.createEntityManager().close();
        }
        catch ( Exception e )
        {
            throw new SQLException( e );
        }
    }


    public PluginDataService( final EntityManagerFactory emf, final GsonBuilder gsonBuilder )
    {
        Preconditions.checkNotNull( emf, "EntityManagerFactory cannot be null." );
        Preconditions.checkNotNull( gsonBuilder, "GsonBuilder cannot be null." );

        this.emf = emf;
        gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().create();
    }


    public void update( String source, String key, final Object info ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            ClusterDataEntity entity = new ClusterDataEntity( source, key, info.toString() );
            em.merge( entity );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            throw new SQLException( e );
        }
        finally
        {
            em.close();
        }
    }


    public <T> List<T> getInfo( String source, final Class<T> clazz ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();
        List<T> result = new ArrayList<>();
        try
        {
            source = source.toUpperCase();
            em.getTransaction().begin();

            List<String> infoList =
                    em.createQuery( "select cd.info from ClusterDataEntity cd where cd.source = :source", String.class )
                      .setParameter( "source", source ).getResultList();
            for ( final String info : infoList )
            {
                result.add( gson.fromJson( info, clazz ) );
            }

            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            throw new SQLException( e );
        }
        finally
        {
            em.close();
        }
        return result;
    }


    public <T> T getInfo( String source, String key, final Class<T> clazz ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();
        T result = null;
        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            TypedQuery<String> query = em.createQuery(
                    "select cd.info from ClusterDataEntity cd where cd.source = :source and cd.id = :id",
                    String.class );
            query.setParameter( "source", source );
            query.setParameter( "id", key );

            String info = query.getSingleResult();
            result = gson.fromJson( info, clazz );
            em.getTransaction().commit();
        }
        catch ( NoResultException e )
        {
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            throw new SQLException( e );
        }
        finally
        {
            em.close();
        }
        return result;
    }


    public void remove( String source, String key ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            Query query =
                    em.createQuery( "DELETE FROM ClusterDataEntity cd WHERE cd.source = :source and cd.key = :key" );
            query.setParameter( "source", source );
            query.setParameter( "id", key );
            query.executeUpdate();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            throw new SQLException( e );
        }
        finally
        {
            em.close();
        }
    }
}

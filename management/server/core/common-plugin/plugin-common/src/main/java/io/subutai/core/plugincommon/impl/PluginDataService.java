package io.subutai.core.plugincommon.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.plugincommon.model.ClusterDataEntity;


public class PluginDataService
{
    private EntityManagerFactory emf;
    private Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = LoggerFactory.getLogger( PluginDataService.class );
    private IdentityManager identityManager;


    public PluginDataService( final EntityManagerFactory emf ) throws SQLException
    {
        init();
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
        init();
        this.emf = emf;
        gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().create();
    }


    public void init()
    {
        ServiceLocator serviceLocator = new ServiceLocator();

        identityManager = serviceLocator.getService( IdentityManager.class );
    }


    //**********************************************
    private boolean checkActiveUser( User user )
    {
        return identityManager.isUserPermitted( user, PermissionObject.EnvironmentManagement, PermissionScope.ALL_SCOPE,
                PermissionOperation.Read );
    }
    //**********************************************


    private long getActiveUserId()
    {
        long userId = 0;

        try
        {
            User user = identityManager.getActiveUser();

            if ( user == null )
            {
                userId = 0;
            }
            else
            {
                userId = user.getId();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Could not retrieve current user!" );
            return 0;
        }

        return userId;
    }
    //**********************************************


    public void update( String source, String key, final Object info ) throws SQLException

    {
        String infoJson = gson.toJson( info );
        EntityManager em = emf.createEntityManager();

        long userId = getActiveUserId();

        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            ClusterDataEntity entity = new ClusterDataEntity( source, key, infoJson, userId );
            em.merge( entity );
            em.flush();
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


    public void update( String source, String key, final String info ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();

        long userId = getActiveUserId();

        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            ClusterDataEntity entity = new ClusterDataEntity( source, key, info, userId );
            em.merge( entity );
            em.flush();
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

        boolean isAdmin = true;

        long userId = getActiveUserId();

        if ( userId == 0 )
        {
            isAdmin = true;
        }

        try
        {
            source = source.toUpperCase();
            em.getTransaction().begin();

            TypedQuery<String> typedQuery = em.createQuery(
                    "select cd.info from ClusterDataEntity cd where cd.source = :source" + ( isAdmin ? "" :
                                                                                             " and cd.userId = "
                                                                                                     + ":userId" ),
                    String.class );
            typedQuery.setParameter( "source", source );
            if ( !isAdmin )
            {
                typedQuery.setParameter( "userId", userId );
            }

            List<String> infoList = typedQuery.getResultList();

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

        boolean isAdmin = true;

        long userId = getActiveUserId();

        if ( userId == 0 )
        {
            isAdmin = true;
        }

        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            TypedQuery<String> query = em.createQuery(
                    "select cd.info from ClusterDataEntity cd where cd.source = :source and cd.id = :id" + ( isAdmin ?
                                                                                                             "" :
                                                                                                             " and cd.userId = :userId" ),
                    String.class );
            if ( !isAdmin )
            {
                query.setParameter( "userId", userId );
            }
            query.setParameter( "source", source );
            query.setParameter( "id", key );

            List<String> infoList = query.getResultList();
            if ( !infoList.isEmpty() )
            {
                result = gson.fromJson( infoList.get( 0 ), clazz );
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


    public List<String> getInfo( String source ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();
        List<String> result = new ArrayList<>();

        boolean isAdmin = true;

        long userId = getActiveUserId();

        if ( userId == 0 )
        {
            isAdmin = true;
        }

        try
        {
            source = source.toUpperCase();
            em.getTransaction().begin();

            TypedQuery<String> query = em.createQuery(
                    "select cd.info from ClusterDataEntity cd where cd.source = :source" + ( isAdmin ? "" :
                                                                                             " and cd.userId = "
                                                                                                     + ":userId" ),
                    String.class );

            query.setParameter( "source", source );
            if ( !isAdmin )
            {
                query.setParameter( "userId", userId );
            }
            result = query.getResultList();
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


    public String getInfo( String source, String key ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();

        String result = null;

        boolean isAdmin = true;

        long userId = getActiveUserId();

        if ( userId == 0 )
        {
            isAdmin = true;
        }

        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            TypedQuery<String> query = em.createQuery(
                    "select cd.info from ClusterDataEntity cd where cd.source = :source and cd.id = :id" + ( isAdmin ?
                                                                                                             "" :
                                                                                                             " and cd.userId = :userId" ),
                    String.class );
            query.setParameter( "source", source );
            query.setParameter( "id", key );
            if ( !isAdmin )
            {
                query.setParameter( "userId", userId );
            }

            List<String> infoList = query.getResultList();
            if ( !infoList.isEmpty() )
            {
                result = infoList.get( 0 );
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


    public void remove( String source, String key ) throws SQLException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            source = source.toUpperCase();
            key = key.toUpperCase();
            em.getTransaction().begin();
            Query query =
                    em.createQuery( "DELETE FROM ClusterDataEntity cd WHERE cd.source = :source and cd.id = :id" );
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


    public void setIdentityManager( IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }
}

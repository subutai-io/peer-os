package io.subutai.core.peer.impl.dao;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.util.GsonInterfaceAdapter;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.impl.entity.PeerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * PeerDao is used to manage peer hosts metadata in database
 */
public class PeerDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerDAO.class );
    private DaoManager daoManager;
    protected Gson gson;


    public PeerDAO( final DaoManager daoManager ) throws SQLException
    {
        Preconditions.checkNotNull( daoManager, "DaoManager is null" );

        this.daoManager = daoManager;
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
        gsonBuilder.registerTypeAdapter( ManagementHost.class, new GsonInterfaceAdapter<ManagementHost>() ).create();
        gson = gsonBuilder.create();
    }


    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            String json = gson.toJson( info );
            PeerData peerData = new PeerData();
            peerData.setId( key );
            peerData.setSource( source );
            peerData.setInfo( json );

            daoManager.startTransaction( entityManager );
            entityManager.merge( peerData );
            daoManager.commitTransaction( entityManager );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            daoManager.rollBackTransaction( entityManager );
            return false;
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return true;
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        List<T> list = new ArrayList<>();
        try
        {
            TypedQuery<PeerData> query;
            query = entityManager
                    .createQuery( "SELECT pd FROM PeerData AS pd WHERE pd.source = :source", PeerData.class );
            query.setParameter( "source", source );
            List<PeerData> results = query.getResultList();

            for ( PeerData pd : results )
            {
                list.add( gson.fromJson( pd.getInfo(), clazz ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return list;
    }


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     */
    public <T> T getInfo( String source, String key, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {

            TypedQuery query;
            query = entityManager.createQuery( "SELECT pd FROM PeerData AS pd WHERE pd.source = :source and pd.id=:id",
                    PeerData.class );
            query.setParameter( "source", source );
            query.setParameter( "id", key );
            PeerData pd = ( PeerData ) query.getSingleResult();

            if ( pd != null )
            {
                return gson.fromJson( pd.getInfo(), clazz );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }

        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public boolean deleteInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {

            daoManager.startTransaction( entityManager );

            Query query;
            query = entityManager.createQuery( "delete FROM PeerData "
                    + "                         AS pd WHERE pd.source = :source and pd.id=:id" );
            query.setParameter( "source", source );
            query.setParameter( "id", key );
            query.executeUpdate();
            daoManager.commitTransaction( entityManager );
            return true;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            daoManager.rollBackTransaction( entityManager );
            return false;
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
    }
}

package org.safehaus.subutai.core.environment.impl.dao;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentBlueprintEntity;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentBuildProcessEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class EnvironmentDAO
{

    private static final  Logger LOG = LoggerFactory.getLogger( EnvironmentDAO.class.getName() );
    private static final  Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final  String ERR_NO_SOURCE = "Source is null or empty";
    private static final  String ERR_NO_KEY = "Key is null or empty";
    private DaoManager    daoManager;

    protected DbUtil dbUtil;


    public EnvironmentDAO( DaoManager daoManager ) throws SQLException
    {
        this.daoManager = daoManager;
    }

    public boolean saveInfo( String source, String key, Object info ) throws EnvironmentPersistenceException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), ERR_NO_SOURCE );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), ERR_NO_KEY );
        Preconditions.checkNotNull( info, "Info is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            //dbUtil.update( "merge into environment (source, id, info) values (? , ?, ?)", source,
            // UUID.fromString( key ), GSON.toJson( info ) );

            EnvironmentBuildProcessEntity environmentBuildProcessEntity = new EnvironmentBuildProcessEntity();
            environmentBuildProcessEntity.setId(  key );
            environmentBuildProcessEntity.setSource( source );
            environmentBuildProcessEntity.setInfo( GSON.toJson( info ) );

            daoManager.startTransaction( entityManager );
            entityManager.merge( environmentBuildProcessEntity );
            daoManager.commitTransaction( entityManager );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );

            daoManager.rollBackTransaction( entityManager );
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
            return true;
        }
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List getInfo( String source, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), ERR_NO_SOURCE );
        Preconditions.checkNotNull( clazz, "Class is null" );

        List<T> list   = new ArrayList<>();
        EntityManager entityManager = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {   /*
            ResultSet rs = dbUtil.select( "select info from environment where source = ?", source );
            while ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    list.add( GSON.fromJson( info, clazz ) );
                }
            }*/

            Query query;
            query = entityManager.createQuery( "SELECT ebp FROM EnvironmentBuildProcessEntity "
                    + "                             AS ebp WHERE ebp.source = :source" );
            query.setParameter( "source" ,source );
            List<EnvironmentBuildProcessEntity> results = query.getResultList();

            for (EnvironmentBuildProcessEntity ebp : results)
            {
                list.add( GSON.fromJson( ebp.getInfo(), clazz ) );
            }

        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), ERR_NO_SOURCE );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), ERR_NO_KEY );
        Preconditions.checkNotNull( clazz, "Class is null" );

        EntityManager entityManager = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            /*
            ResultSet rs = dbUtil.select( "select info from environment where source = ? and id = ?", source,
                                          UUID.fromString( key ) );
            if ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    return GSON.fromJson( info, clazz );
                }
            }*/
            Query query;
            query = entityManager.createQuery( "SELECT ebp FROM EnvironmentBuildProcessEntity "
                    + "                             AS ebp WHERE ebp.source = :source and ebp.id=:id" );
            query.setParameter( "source" ,source );
            query.setParameter( "id", key );
            EnvironmentBuildProcessEntity ebp = (EnvironmentBuildProcessEntity) query.getSingleResult();

            if(ebp !=null)
            {
                daoManager.closeEntityManager( entityManager );
                return GSON.fromJson( ebp.getInfo(), clazz );
            }

        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
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
    public synchronized boolean deleteInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), ERR_NO_SOURCE );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), ERR_NO_KEY );
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            //dbUtil.update( "delete from environment where source = ? and id = ?", source, UUID.fromString( key ) );
            daoManager.startTransaction( entityManager );

            Query query;
            query = entityManager.createQuery( "delete FROM EnvironmentBuildProcessEntity "
                    + "                         AS ebp WHERE ebp.source = :source and ebp.id=:id" );
            query.setParameter( "source" ,source );
            query.setParameter( "id", key  );
            query.executeUpdate();
            daoManager.commitTransaction( entityManager );
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
            return true;
        }
    }


    public UUID saveBlueprint( final EnvironmentBlueprint blueprint ) throws EnvironmentPersistenceException
    {
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();
        UUID bpId;
        try
        {
            //dbUtil.update( "merge into blueprint (id, info) values (? , ?)", blueprint.getId(), json );


            String json = GSON.toJson( blueprint );

            EnvironmentBlueprintEntity environmentBlueprintEntity = new  EnvironmentBlueprintEntity();
            environmentBlueprintEntity.setId( blueprint.getId().toString() );
            environmentBlueprintEntity.setInfo(json );

            daoManager.startTransaction( entityManager );
            entityManager.merge( environmentBlueprintEntity );
            daoManager.commitTransaction( entityManager );

            bpId = blueprint.getId();
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to save blueprint", e );
            daoManager.rollBackTransaction( entityManager );
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }

        return bpId;
    }


    public List<EnvironmentBlueprint> getBlueprints() throws EnvironmentPersistenceException
    {
        List<EnvironmentBlueprint> blueprints = new ArrayList<>();
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {   /*
            ResultSet rs = dbUtil.select( "select info from blueprint" );
            while ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    blueprints.add( GSON.fromJson( info, EnvironmentBlueprint.class ) );
                }
            }*/

            Query query;
            query = entityManager.createQuery( "SELECT ebe FROM EnvironmentBlueprintEntity AS ebe" );

            List<EnvironmentBlueprintEntity> results = query.getResultList();

            for (EnvironmentBlueprintEntity ebe : results)
            {
                blueprints.add( GSON.fromJson( ebe.getInfo(), EnvironmentBlueprint.class ) );
            }

        }
        catch ( Exception e )
        {
            LOG.error( "Failed to get blueprints", e );
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return blueprints;
    }


    public boolean deleteBlueprint( final UUID blueprintId ) throws EnvironmentPersistenceException
    {
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( entityManager );

            Query query;
            query = entityManager.createQuery( "delete FROM EnvironmentBlueprintEntity "
                    + "                         AS ebe WHERE ebe.id=:id" );
            query.setParameter( "id",blueprintId.toString() );
            query.executeUpdate();
            daoManager.commitTransaction( entityManager );

            return true;
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to delete blueprint", e );
            daoManager.rollBackTransaction( entityManager );
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
    }


    public EnvironmentBlueprint getBlueprint( UUID blueprintId ) throws EnvironmentPersistenceException
    {
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            EnvironmentBlueprintEntity ebe = entityManager.find(EnvironmentBlueprintEntity.class,blueprintId.toString());

            if(ebe!=null)
            {
                return GSON.fromJson( ebe.getInfo() , EnvironmentBlueprint.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return null;
    }
}


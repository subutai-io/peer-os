package io.subutai.core.tracker.rest;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperationView;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.tracker.rest.pojo.VersionPojo;


/**
 *
 */

public class RestServiceImpl implements RestService
{

    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Tracker tracker;


    public RestServiceImpl( final Tracker tracker )
    {
        Preconditions.checkNotNull( tracker, "Tracker is null" );

        this.tracker = tracker;
    }


    @Override
    public Response getTrackerOperation( final String source, final String uuid )
    {
        try
        {
            UUID poUUID = UUID.fromString( uuid );

            TrackerOperationView trackerOperationView = tracker.getTrackerOperation( source, poUUID );

            if ( trackerOperationView != null )
            {
                return Response.ok().entity( GSON.toJson( trackerOperationView ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( NullPointerException | IllegalArgumentException e )
        {
            LOG.error( "Error in getTrackerOperation", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getTrackerOperations( final String source, final String fromDate, final String toDate,
                                          final int limit )
    {
        try
        {
            SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            Date fromDat = df.parse( fromDate + " 00:00:00" );
            Date toDat = df.parse( toDate + " 23:59:59" );

            List<TrackerOperationView> pos = tracker.getTrackerOperations( source, fromDat, toDat, limit );

            return Response.ok().entity( GSON.toJson( pos ) ).build();
        }
        catch ( ParseException e )
        {
            LOG.error( "Error in getTrackerOperations", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getTrackerOperationSources()
    {
        return Response.ok().entity( GSON.toJson( tracker.getTrackerOperationSources() ) ).build();
    }


    @Override
    public Response getSubutaiInfo()
    {
        Properties prop = new Properties();
        VersionPojo pojo = new VersionPojo();
        InputStream input = null;
        try
        {
            input = new FileInputStream( String.format( "%s/git.properties", Common.KARAF_ETC ) );

            prop.load( input );
            pojo.setProjectVersion( prop.getProperty( "git.build.version" ) );
            pojo.setGitBuildUserEmail( prop.getProperty( "git.build.user.email" ) );
            pojo.setGitBuildHost( prop.getProperty( "git.build.host" ) );
            pojo.setGitClosestTagName( prop.getProperty( "git.closest.tag.name" ) );
            pojo.setGitCommitIdDescribeShort( prop.getProperty( "git.commit.id.describe-short" ) );
            pojo.setGitCommitTime( prop.getProperty( "git.commit.time" ) );
            pojo.setGitBranch( prop.getProperty( "git.branch" ) );
            pojo.setGitBuildUserName( prop.getProperty( "git.build.user.name" ) );
            pojo.setGitClosestTagCommitCount( prop.getProperty( "git.closest.tag.commit.count" ) );
            pojo.setGitCommitIdDescribe( prop.getProperty( "git.commit.id.describe" ) );
            pojo.setGitCommitId( prop.getProperty( "git.commit.id" ) );
            pojo.setGitBuildTime( prop.getProperty( "git.build.time" ) );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        finally
        {
            if ( input != null )
            {
                try
                {
                    input.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }

        String projectInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( projectInfo ).build();
    }
}

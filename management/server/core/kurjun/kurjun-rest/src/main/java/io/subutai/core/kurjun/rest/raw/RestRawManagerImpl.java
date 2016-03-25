package io.subutai.core.kurjun.rest.raw;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ai.subut.kurjun.metadata.common.raw.RawMetadata;
import ai.subut.kurjun.model.metadata.Metadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import io.subutai.core.kurjun.api.Utils;
import io.subutai.core.kurjun.api.raw.RawManager;
import io.subutai.core.kurjun.rest.RestManagerBase;


public class RestRawManagerImpl extends RestManagerBase implements RestRawManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( RestRawManagerImpl.class );

    private static final Gson GSON = new GsonBuilder().create();

    private final RawManager rawManager;


    public RestRawManagerImpl( RawManager rawManager )
    {
        this.rawManager = rawManager;
    }


    @Override
    public Response download( String repository, String md5, String name, boolean isKurjunClient )
    {
        if ( repository == null )
        {
            repository = "raw";
        }

        try
        {

            byte[] md5bytes = decodeMd5( md5 );

            RawMetadata raw = rawManager.getInfo( repository, md5bytes );

            if ( raw != null )
            {
                InputStream is = rawManager.getFile( repository, md5bytes );

                if ( is != null )
                {
                    return Response.ok( is ).header( "Content-Disposition", "attachment; filename=" + raw.getName() )
                                   .header( "Content-Type", "application/octet-stream" )
                                   .header( "Content-Length", raw.getSize() ).build();
                }
            }
        }
        catch ( IllegalArgumentException ex )
        {
            LOGGER.error( "", ex );

            return badRequest( ex.getMessage() );
        }
        catch ( IOException ex )
        {
            String msg = "Failed to get file info";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
        }
        return packageNotFoundResponse();
    }


    @Override
    public Response info( String repository, String md5, String name, boolean isKurjunClient )
    {
        try
        {
            RawMetadata rawMetadata = new RawMetadata();

            if ( repository == null && md5 != null )
            {
                repository = "raw";
            }
            rawMetadata.setName( name );
            rawMetadata.setMd5Sum( Utils.MD5.toByteArray( md5 ) );
            rawMetadata.setFingerprint( repository );

            Metadata metadata = rawManager.getInfo( repository, decodeMd5( md5 ) );

            if ( metadata != null )
            {
                return Response.ok( GSON.toJson( metadata ) ).build();
            }
        }
        catch ( IllegalArgumentException ex )
        {
            LOGGER.error( "", ex );
            return badRequest( ex.getMessage() );
        }

        return packageNotFoundResponse();
    }


    @Override
    public Response list( String repository )
    {
        try
        {
            List<SerializableMetadata> list = rawManager.list( repository );

            if ( list != null )
            {

                return Response.ok( GSON.toJson( list ) ).build();
            }
        }
        catch ( IOException ex )
        {
            String msg = "Failed to get file list";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
        }
        return Response.ok( "No files" ).build();
    }


    @Override
    public Response upload( String repository, Attachment attachment )
    {
        File temp = null;
        try
        {
            String filename = attachment.getContentDisposition().getParameter( "filename" );

            if ( filename == null || filename.trim().isEmpty() )
            {
                throw new IllegalArgumentException( "filename is required" );
            }

            temp = Files.createTempFile( null, null ).toFile();
            attachment.transferTo( temp );

            RawMetadata rawMetadata = rawManager.put( temp, filename, repository );

            if ( rawMetadata != null )
            {
                return Response.ok( rawMetadata ).build();
            }
            else
            {
                return Response.serverError().entity( "Failed to put file" ).build();
            }
        }
        catch ( IllegalArgumentException ex )
        {
            String msg = "Failed to put file";
            LOGGER.error( msg, ex );
            return badRequest( msg + " " + ex.getMessage() );
        }
        catch ( IOException ex )
        {
            String msg = "Failed to put file";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
        }
        finally
        {
            FileUtils.deleteQuietly( temp );
        }
    }


    @Override
    public Response delete( String repository, String md5 )
    {

        try
        {
            byte[] md5bytes = decodeMd5( md5 );

            if ( md5bytes != null )
            {
                boolean deleted = rawManager.delete( repository, md5bytes );
                if ( deleted )
                {
                    return Response.ok( "File deleted" ).build();
                }
                else
                {
                    return packageNotFoundResponse();
                }
            }
            return badRequest( "Invalid md5 checksum" );
        }
        catch ( IllegalArgumentException ex )
        {
            LOGGER.error( "", ex );
            return badRequest( ex.getMessage() );
        }
    }


    @Override
    public Response md5( final String repository )
    {
        return Response.ok( rawManager.md5() ).build();
    }


    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }
}

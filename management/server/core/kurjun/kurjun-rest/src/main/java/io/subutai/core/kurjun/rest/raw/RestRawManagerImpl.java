package io.subutai.core.kurjun.rest.raw;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ai.subut.kurjun.metadata.common.raw.RawMetadata;
import io.subutai.common.protocol.Resource;
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
    public Response getFile( String md5, String name, boolean isKurjunClient )
    {
        try
        {
            byte[] md5bytes = decodeMd5( md5 );
            Resource raw;

            if ( md5bytes != null )
            {
                raw = rawManager.getFile( md5bytes, isKurjunClient );
            }
            else
            {
                raw = rawManager.getFile( name, isKurjunClient );
            }

            if ( raw != null )
            {
                InputStream is = rawManager.getFileData( decodeMd5( raw.getMd5Sum() ), isKurjunClient );
                if ( is != null )
                {
                    return Response.ok( is ).header( "Content-Disposition",
                            "attachment; filename=" + raw.getName() )
                            .header( "Content-Type", "application/octet-stream" ).build();
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
    public Response getFileInfo( String md5, String name, boolean isKurjunClient )
    {
        try
        {
            byte[] md5bytes = decodeMd5( md5 );
            Resource raw;
            if ( md5bytes != null )
            {
                raw = rawManager.getFile( md5bytes, isKurjunClient );
            }
            else
            {
                raw = rawManager.getFile( name, isKurjunClient );
            }

            if ( raw != null )
            {
                return Response.ok( GSON.toJson( convertToRawMetadata( raw ) ) ).build();
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
    public Response getFileList( boolean isKurjunClient )
    {
        try
        {
            List<Resource> list = rawManager.getFileList( isKurjunClient );

            if ( list != null )
            {
                List<RawMetadata> deflist
                        = list.stream().map( t -> convertToRawMetadata( t ) ).collect( Collectors.toList() );
                return Response.ok( GSON.toJson( deflist ) ).build();
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
    public Response uploadFile( Attachment attachment )
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

            try ( InputStream is = new FileInputStream( temp ) )
            {
                String md5 = rawManager.uploadFile( is, filename );
                if ( md5 != null )
                {
                    return Response.ok( md5 ).build();
                }
                else
                {
                    return Response.serverError().entity( "Failed to put file" ).build();
                }
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
    public Response deleteFile( String md5 )
    {
        try
        {
            byte[] md5bytes = decodeMd5( md5 );
            if ( md5bytes != null )
            {
                try
                {
                    boolean deleted = rawManager.deleteFile( md5bytes );
                    if ( deleted )
                    {
                        return Response.ok( "File deleted" ).build();
                    }
                    else
                    {
                        return packageNotFoundResponse();
                    }
                }
                catch ( IOException ex )
                {
                    String err = "Failed to delete file";
                    LOGGER.error( err, ex );
                    return Response.serverError().entity( err ).build();
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
    protected Logger getLogger()
    {
        return LOGGER;
    }


    private RawMetadata convertToRawMetadata( Resource raw )
    {
        RawMetadata defaultTemplate = new RawMetadata( decodeMd5( raw.getMd5Sum() ), raw.getName(), raw.getSize() );
        return defaultTemplate;
    }

}

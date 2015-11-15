package io.subutai.core.kurjun.rest;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.geronimo.mail.util.Hex;

import io.subutai.core.kurjun.api.TemplateManager;


public class RestTemplateManagerImpl implements RestTemplateManager
{


    private static final Logger LOGGER = LoggerFactory.getLogger( RestTemplateManagerImpl.class );

    private final TemplateManager templateManager;


    public RestTemplateManagerImpl( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    public Response getTemplate( String repository, String md5, String name, String version, String type )
    {
        try
        {
            byte[] md5bytes = decodeMd5( md5 );
            if ( md5bytes != null )
            {
                InputStream is = templateManager.getTemplate( repository, md5bytes );
                if ( is != null )
                {
                    return Response.ok( is ).build();
                }
            }

            String json = templateManager.getTemplateInfo( repository, name, version );
            if ( json != null )
            {
                return Response.ok( json ).build();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to get template info", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
        return packageNotFoundResponse();
    }


    @Override
    public Response uploadTemplate( String repository, Attachment attachment )
    {
        File temp = null;
        try
        {
            temp = Files.createTempFile( null, null ).toFile();
            attachment.transferTo( temp );

            try ( InputStream is = new FileInputStream( temp ) )
            {
                byte[] md5 = templateManager.upload( repository, is );
                return Response.ok( Hex.encode( md5 ) ).build();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to put template", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
        finally
        {
            FileUtils.deleteQuietly( temp );
        }
    }


    @Override
    public Response deleteTemplates( String repository, String md5 )
    {
        byte[] md5bytes = decodeMd5( md5 );
        if ( md5bytes != null )
        {
            String err = "Failed to delete templates";
            try
            {
                boolean deleted = templateManager.delete( repository, md5bytes );
                if ( deleted )
                {
                    return Response.ok( "Template deleted" ).build();
                }
                return Response.serverError().entity( err ).build();
            }
            catch ( IOException ex )
            {
                LOGGER.error( err, ex );
                return Response.serverError().entity( err ).build();
            }
        }
        return badRequest( "Invalid md5 checksum" );
    }


    private byte[] decodeMd5( String md5 )
    {
        if ( md5 != null )
        {
            try
            {
                return Hex.decode( md5 );
            }
            catch ( RuntimeException ex )
            {
                LOGGER.error( "Invalid md5 checksum", ex );
            }
        }
        return null;
    }


    protected Response badRequest( String msg )
    {
        return Response.status( Response.Status.BAD_REQUEST ).entity( msg ).build();
    }


    protected Response notFoundResponse( String msg )
    {
        return Response.status( Response.Status.NOT_FOUND ).entity( msg ).build();
    }


    protected Response packageNotFoundResponse()
    {
        return notFoundResponse( "Package not found." );
    }


    protected Response forbiddenResponse( String msg )
    {
        return Response.status( Response.Status.FORBIDDEN ).entity( msg ).build();
    }


    protected Response forbiddenResponse()
    {
        return forbiddenResponse( "No permission." );
    }
}


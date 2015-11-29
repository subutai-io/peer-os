package io.subutai.core.kurjun.rest.template;


import ai.subut.kurjun.metadata.common.subutai.DefaultTemplate;
import ai.subut.kurjun.model.metadata.Architecture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.core.kurjun.rest.RestManagerBase;
import java.util.List;
import java.util.stream.Collectors;


public class RestTemplateManagerImpl extends RestManagerBase implements RestTemplateManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( RestTemplateManagerImpl.class );

    private static final Gson GSON = new GsonBuilder().create();

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
                TemplateKurjun template = templateManager.getTemplate( repository, md5bytes );
                InputStream is = templateManager.getTemplateData( repository, md5bytes );
                if ( template != null && is != null )
                {
                    return Response.ok( is )
                            .header( "Content-Disposition", "attachment; filename=" + makeFilename( template ) )
                            .header( "Content-Type", "application/octet-stream" )
                            .build();
                }
            }
            else
            {
                TemplateKurjun template = templateManager.getTemplate( repository, name, version );

                if ( template != null && RestTemplateManager.RESPONSE_TYPE_MD5.equals( type ) )
                {
                    return Response.ok( template.getMd5Sum() ).build();
                }
            }
        }
        catch ( IOException ex )
        {
            String msg = "Failed to get template info";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
        }
        return packageNotFoundResponse();
    }


    @Override
    public Response getTemplateInfo( String repository, String md5, String name, String version )
    {
        try
        {
            byte[] md5bytes = decodeMd5( md5 );
            if ( md5bytes != null )
            {
                TemplateKurjun template = templateManager.getTemplate( repository, md5bytes );
                if ( template != null )
                {
                    return Response.ok( GSON.toJson( convertToDefaultTemplate( template ) ) ).build();
                }
            }

            TemplateKurjun template = templateManager.getTemplate( repository, name, version );

            if ( template != null )
            {
                return Response.ok( GSON.toJson( convertToDefaultTemplate( template ) ) ).build();
            }
        }
        catch ( IOException ex )
        {
            String msg = "Failed to get template info";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
        }
        return packageNotFoundResponse();
    }


    @Override
    public Response getTemplateList( String repository )
    {
        try
        {
            List<TemplateKurjun> list = templateManager.list( repository );
            if ( list != null )
            {
                List<DefaultTemplate> deflist = list.stream().map( t -> convertToDefaultTemplate( t ) ).collect( Collectors.toList() );
                return Response.ok( GSON.toJson( deflist ) ).build();
            }
        }
        catch ( IOException ex )
        {
            String msg = "Failed to get template list info";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
        }
        return Response.ok( "No templates" ).build();
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
            String msg = "Failed to put template";
            LOGGER.error( msg, ex );
            return Response.serverError().entity( msg ).build();
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


    private DefaultTemplate convertToDefaultTemplate( TemplateKurjun template )
    {
        DefaultTemplate defaultTemplate = new DefaultTemplate();
        defaultTemplate.setName( template.getName() );
        defaultTemplate.setVersion( template.getVersion() );
        defaultTemplate.setMd5Sum( Hex.decode( template.getMd5Sum() ) );
        defaultTemplate.setArchitecture( Architecture.getByValue( template.getArchitecture() ) );
        defaultTemplate.setParent( template.getParent() );
        defaultTemplate.setPackage( template.getPackageName() );
        return defaultTemplate;
    }


    private String makeFilename( TemplateKurjun t )
    {
        return t.getName() + "_" + t.getVersion() + ".tar.gz";
    }


    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

}

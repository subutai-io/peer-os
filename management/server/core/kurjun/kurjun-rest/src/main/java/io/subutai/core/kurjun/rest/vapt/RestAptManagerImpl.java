package io.subutai.core.kurjun.rest.vapt;


import ai.subut.kurjun.metadata.common.apt.DefaultPackageMetadata;
import ai.subut.kurjun.metadata.common.utils.MetadataUtils;
import ai.subut.kurjun.model.metadata.apt.PackageMetadata;
import io.subutai.core.kurjun.api.vapt.AptManager;
import io.subutai.core.kurjun.rest.RestManagerBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestAptManagerImpl extends RestManagerBase implements RestAptManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( RestAptManagerImpl.class );

    private final AptManager aptManager;


    public RestAptManagerImpl( AptManager aptManager )
    {
        this.aptManager = aptManager;
    }


    @Override
    public Response getRelease( String release )
    {
        String releaseIndex = aptManager.getRelease( release, null, null );
        return ( releaseIndex != null ) ? Response.ok( releaseIndex ).build() : notFoundResponse( "Release not found." );
    }


    @Override
    public Response getPackagesIndex( String release, String component, String arch, String packagesIndex )
    {
        try
        {
            InputStream is = aptManager.getPackagesIndex( release, component, arch, packagesIndex );

            Response.ResponseBuilder rb = Response.ok();

            rb.header( "Content-Type", "application/octet-stream" );

            if ( aptManager.isCompressionTypeSupported( packagesIndex ) )
            {
                // make archived package indices downloadable by specifying content disposition header
                rb.header( "Content-Disposition", " attachment; filename=" + packagesIndex );
            }
            return rb.entity( is ).build();
        }
        catch ( IllegalArgumentException e )
        {
            return notFoundResponse( e.getMessage() );
        }
    }


    @Override
    public Response getPackageByFilename( String filename )
    {
        try
        {
            String serialized = aptManager.getSerializedPackageInfo( filename );

            if ( serialized != null )
            {
                InputStream is = aptManager.getPackageByFilename( filename );
                if ( is != null )
                {
                    DefaultPackageMetadata pm = MetadataUtils.JSON.fromJson( serialized, DefaultPackageMetadata.class );
                    return Response.ok( is )
                            .header( "Content-Disposition", "attachment; filename=" + makePackageFilename( pm ) )
                            .header( "Content-Type", "application/octet-stream" )
                            .build();
                }
            }
        }
        catch ( IllegalArgumentException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
        return packageNotFoundResponse();
    }


    @Override
    public Response upload( Attachment attachment )
    {
//        if ( checkAuthentication( Permission.ADD_PACKAGE ) )
//        {
//            return forbiddenResponse();
//        }

        File temp = null;
        try
        {
            temp = Files.createTempFile( "deb-upload", null ).toFile();
            attachment.transferTo( temp );
            try ( InputStream is = new FileInputStream( temp ) )
            {
                URI location = aptManager.upload( is );
                if ( location != null )
                {
                    return Response.created( location ).build();
                }
            }
        }
        catch ( IOException | IllegalArgumentException ex )
        {
            LOGGER.error( "Failed to upload", ex );
        }
        finally
        {
            FileUtils.deleteQuietly( temp );
        }
        return Response.serverError().entity( "Failed to upload package." ).build();
    }


    @Override
    public Response getPackageInfo( String md5, String name, String version )
    {
//        if ( checkAuthentication( Permission.GET_PACKAGE ) )
//        {
//            return forbiddenResponse();
//        }

        String str = aptManager.getPackageInfo( decodeMd5( md5 ), name, version );

        if ( str != null )
        {
            return Response.ok( str, MediaType.APPLICATION_JSON ).build();
        }
        return packageNotFoundResponse();
    }


    @Override
    public Response getPackage( String md5 )
    {
//        if ( checkAuthentication( Permission.GET_PACKAGE ) )
//        {
//            return forbiddenResponse();
//        }

        String serialized = aptManager.getSerializedPackageInfo( decodeMd5( md5 ) );

        if ( serialized != null )
        {
            InputStream is = aptManager.getPackage( decodeMd5( md5 ) );

            if ( is != null )
            {
                DefaultPackageMetadata pm = MetadataUtils.JSON.fromJson( serialized, DefaultPackageMetadata.class );
                return Response.ok( is )
                        .header( "Content-Disposition", "attachment; filename=" + makePackageFilename( pm ) )
                        .header( "Content-Type", "application/octet-stream" )
                        .build();
            }
        }
        return packageNotFoundResponse();
    }


    private String makePackageFilename( PackageMetadata metadata )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( metadata.getPackage() ).append( "_" );
        sb.append( metadata.getVersion() ).append( "_" );
        sb.append( metadata.getArchitecture().toString() );
        sb.append( ".deb" );
        return sb.toString();
    }


//    @Override
//    protected AuthManager getAuthManager()
//    {
//        return authManager;
//    }
    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

}

package io.subutai.core.kurjun.rest;


import ai.subut.kurjun.model.metadata.Metadata;
import io.subutai.core.kurjun.api.TemplateManager;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestTemplateManagerImpl implements RestTemplateManager
{

    @Override
    public Response uploadTemplate( String repository, Attachment attachment )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Response deleteTemplates( String repository, String md5 )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    private static final Logger LOGGER = LoggerFactory.getLogger( RestTemplateManagerImpl.class );

    private final TemplateManager templateManager;


    public RestTemplateManagerImpl( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    public Response getTemplate( String repository, String md5, String name, String version, String type )
    {
        if ( true )
        {
            LOGGER.error( "REEEESTTTT" );
            LOGGER.error( "templateManager = " + templateManager );
            
            templateManager.getTemplate( repository, md5, name, version, type );

            throw new RuntimeException( "REEEESTTTT EXCEPTION" );
        }

        return badRequest( "Name or type patameters not specified" );

    }
//
//
//    @Override
//    public Response uploadTemplate( String repository, Attachment attachment )
//    {
//
//        KurjunContext context = getContextByRepoType( repository );
//        if ( context == null )
//        {
//            return badRequest( "Invalid template repository: " + repository );
//        }
//        try
//        {
//            parsePackageFile( attachment, context );
//            return Response.ok( "Template uploaded" ).build();
//        }
//        catch ( IOException ex )
//        {
//            return Response.serverError().entity( ex ).build();
//        }
//    }
//
//
//    @Override
//    public Response deleteTemplates( String repository, String md5hex )
//    {
//        KurjunContext context = getContextByRepoType( repository );
//        if ( context == null )
//        {
//            return badRequest( "Invalid template repository: " + repository );
//        }
//        byte[] md5;
//        try
//        {
//            md5 = Hex.decodeHex( md5hex.toCharArray() );
//        }
//        catch ( DecoderException ex )
//        {
//            LOGGER.warn( ex.getMessage() );
//            return badRequest( "Invalid md5 value" );
//        }
//
//        PackageMetadataStore metadataStore = metadataStoreFactory.create( context );
//        try
//        {
//            if ( metadataStore.remove( md5 ) )
//            {
//                FileStore fileStore = fileStoreFactory.create( context );
//                fileStore.remove( md5 );
//            }
//            else
//            {
//                return notFound( "Metadata not found for package md5" );
//            }
//        }
//        catch ( IOException ex )
//        {
//            return Response.serverError().entity( ex ).build();
//        }
//        return Response.ok( "Template deleted" ).build();
//    }
//
//


    private String makeFilename( Metadata m )
    {
        return m.getName() + "_" + m.getVersion() + ".tar.gz";
    }


    private Response badRequest( String message )
    {
        return Response.status( Response.Status.BAD_REQUEST ).entity( message ).build();
    }


    private Response notFound( String message )
    {
        return Response.status( Response.Status.NOT_FOUND ).entity( message ).build();
    }

}

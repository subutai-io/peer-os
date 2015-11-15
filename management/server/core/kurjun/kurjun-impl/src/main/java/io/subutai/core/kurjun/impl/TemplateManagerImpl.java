package io.subutai.core.kurjun.impl;


import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.common.service.KurjunProperties;
import ai.subut.kurjun.index.PackagesIndexParserModule;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreFactory;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.repo.RepositoryModule;
import ai.subut.kurjun.riparser.ReleaseIndexParserModule;
import ai.subut.kurjun.snap.SnapMetadataParserModule;
import ai.subut.kurjun.storage.factory.FileStoreFactory;
import ai.subut.kurjun.storage.factory.FileStoreModule;
import ai.subut.kurjun.subutai.SubutaiTemplateParserModule;
import ai.subut.kurjun.subutai.service.SubutaiTemplateParser;
import com.google.inject.Injector;
import io.subutai.core.kurjun.api.TemplateManager;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TemplateManagerImpl implements TemplateManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );

    private FileStoreFactory fileStoreFactory;
    private PackageMetadataStoreFactory metadataStoreFactory;
    private SubutaiTemplateParser templateParser;

    private static final KurjunContext CONTEXT = new KurjunContext( "my" );
    private static final Set<KurjunContext> TEMPLATE_CONTEXTS = new HashSet<>();


    public void init()
    {

        LOGGER.warn( "INITTTTTTTTTTTTT" );
        Injector injector = bootstrapDI();
        KurjunProperties properties = injector.getInstance( KurjunProperties.class );
        setContexts( properties );

        fileStoreFactory = injector.getInstance( FileStoreFactory.class );
        metadataStoreFactory = injector.getInstance( PackageMetadataStoreFactory.class );
        templateParser = injector.getInstance( SubutaiTemplateParser.class );

//        setPermissions( injector.getInstance( IdentityManager.class ) );
    }


    public void dispose()
    {
        LOGGER.warn( "DISPOOOSE" );

    }


    private Injector bootstrapDI()
    {

        KurjunBootstrap bootstrap = new KurjunBootstrap();
        bootstrap.addModule( new ControlFileParserModule() );
        bootstrap.addModule( new ReleaseIndexParserModule() );
        bootstrap.addModule( new PackagesIndexParserModule() );
        bootstrap.addModule( new SubutaiTemplateParserModule() );

        bootstrap.addModule( new FileStoreModule() );
        bootstrap.addModule( new PackageMetadataStoreModule() );
        bootstrap.addModule( new SnapMetadataParserModule() );

        bootstrap.addModule( new RepositoryModule() );
//        bootstrap.addModule( new SecurityModule() );

        bootstrap.boot();

        return bootstrap.getInjector();
    }


    private static void setContexts( KurjunProperties properties )
    {
        Properties p = properties.getContextProperties( CONTEXT );
        p.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
        //p.setProperty( FileStoreFactory.TYPE, FileStoreFactory.S3 );

        p.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE, PackageMetadataStoreFactory.FILE_DB );

        // init template type contexts based on above parameters
        TEMPLATE_CONTEXTS.add( new KurjunContext( "public" ) );
        TEMPLATE_CONTEXTS.add( new KurjunContext( "trust" ) );
        for ( KurjunContext kc : TEMPLATE_CONTEXTS )
        {
            Properties kcp = properties.getContextProperties( kc );
            kcp.putAll( p );
        }

    }


    @Override
    public void getTemplate( String repository, String md5, String name, String version, String type )
    {
        if ( true )
        {
            LOGGER.error( "IMMPLL" );
            LOGGER.error( "fileStoreFactory = " + fileStoreFactory );
            LOGGER.error( "metadataStoreFactory = " + metadataStoreFactory );
            LOGGER.error( "templateParser = " + templateParser );

            throw new RuntimeException( "IMMPLL EXCEPTION" );
        }

//        KurjunContext context = getContextByRepoType( repository );
//        if ( context == null )
//        {
//            return badRequest( "Invalid template repository: " + repository );
//        }
//        try
//        {
//            if ( md5 != null )
//            {
//                return getByMd5( md5, context );
//            }
//            if ( name != null && type != null )
//            {
//                return getByNameAndVersion( name, version, type, context );
//            }
//            else
//            {
//                return badRequest( "Name or type patameters not specified" );
//            }
//        }
//        catch ( IOException ex )
//        {
//            return Response.serverError().entity( ex ).build();
//        }
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
    @Override
    public void uploadTemplate( String repository, InputStream inputStream )
    {
    }


    @Override
    public void deleteTemplates( String repository, String md5 )
    {
    }


    /**
     * Gets Kurjun context for templates repository type.
     * <p>
     * TODO: looks for contexts defined in {@link HttpServer} main class. Need
     * mechanism independent of the main class.
     *
     * @param type
     * @return
     */
    protected KurjunContext getContextByRepoType( String type )
    {
        Set<KurjunContext> set = TEMPLATE_CONTEXTS;
        for ( Iterator<KurjunContext> it = set.iterator(); it.hasNext(); )
        {
            KurjunContext c = it.next();
            if ( c.getName().equals( type ) )
            {
                return c;
            }
        }
        return null;
    }

//    private Response getByMd5( String md5hex, KurjunContext context ) throws IOException
//    {
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
//        SerializableMetadata meta = metadataStore.get( md5 );
//        if ( meta == null )
//        {
//            return notFound( "Package not found" );
//        }
//
//        FileStore fileStore = fileStoreFactory.create( context );
//        if ( fileStore.contains( md5 ) )
//        {
//            try ( InputStream is = fileStore.get( md5 ) )
//            {
//                return Response.ok( is )
//                        .header( "Content-Disposition", "attachment; filename=" + makeFilename( meta ) )
//                        .build();
//            }
//        }
//        else
//        {
//            return notFound( "Package file not found" );
//        }
//    }
//
//
//    private Response getByNameAndVersion( String name, String version, String type, KurjunContext context ) throws IOException
//    {
//        Objects.requireNonNull( name, "name parameter" );
//        Objects.requireNonNull( type, "type parameter" );
//
//        if ( type.equals( RestTemplateManager.RESPONSE_TYPE_MD5 ) )
//        {
//            return null; //respondMd5( name, version, context );
//        }
//        else
//        {
//            return badRequest( "Invalid type parameter. Specify 'md5' to get md5 checksum of packages." );
//        }
//    }
//
//
//    private Response respondMd5( String name, String version, KurjunContext context ) throws IOException
//    {
//        PackageMetadataStore metadataStore = metadataStoreFactory.create( context );
//        List<SerializableMetadata> items = metadataStore.get( name );
//        if ( items.isEmpty() )
//        {
//            return notFound( "No packages found" );
//        }
//
//        SerializableMetadata meta = null;
//        if ( version != null )
//        {
//            for ( SerializableMetadata item : items )
//            {
//                if ( version.equals( item.getVersion() ) )
//                {
//                    meta = item;
//                    break;
//                }
//            }
//        }
//        else
//        {
//
//            Predicate<String> predicate = ( s ) -> s.length() > 0;
//
//            // alphabetically sort by versions and get latest one
////            Optional<SerializableMetadata> m = items.stream().sorted(
////                    ( m1, m2 ) -> m1.getVersion().compareTo( m2.getVersion() ) )
////                    .skip( items.size() - 1 ).findFirst();
////            if ( m.isPresent() )
////            {
////                meta = m.get();
////            }
//        }
//
//        if ( meta != null )
//        {
//            return Response.ok( Hex.encodeHexString( meta.getMd5Sum() ) ).build();
//        }
//        else
//        {
//            return notFound( "Package not found" );
//        }
//    }
//
//
//    private void parsePackageFile( Attachment attachment, KurjunContext context ) throws IOException
//    {
//        // define file extension based on submitted file name
//        String fileName = attachment.getContentDisposition().getParameter( "filename" );
//        String ext = CompressionType.getExtension( fileName );
//        if ( ext != null )
//        {
//            ext = "." + ext;
//        }
//
//        FileStore fileStore = fileStoreFactory.create( context );
//        PackageMetadataStore metadataStore = metadataStoreFactory.create( context );
//
//        SubutaiTemplateMetadata meta;
//        Path temp = Files.createTempFile( "template-", ext );
//        try ( InputStream is = attachment.getObject( InputStream.class ) )
//        {
//            Files.copy( is, temp, StandardCopyOption.REPLACE_EXISTING );
//            meta = templateParser.parseTemplate( temp.toFile() );
//            fileStore.put( temp.toFile() );
//        }
//        finally
//        {
//            Files.delete( temp );
//        }
//
//        // store meta data separately and catch exception to revert in case meta data storing fails
//        // when package file is already stored
//        try
//        {
//            metadataStore.put( MetadataUtils.serializableTemplateMetadata( meta ) );
//        }
//        catch ( IOException ex )
//        {
//            fileStore.remove( meta.getMd5Sum() );
//            throw ex;
//        }
//    }
//
//
}

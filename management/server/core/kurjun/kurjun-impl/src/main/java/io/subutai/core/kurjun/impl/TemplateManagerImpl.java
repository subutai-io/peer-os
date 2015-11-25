package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.common.service.KurjunProperties;
import ai.subut.kurjun.index.PackagesIndexParserModule;
import ai.subut.kurjun.metadata.common.DefaultMetadata;
import ai.subut.kurjun.metadata.common.subutai.DefaultTemplate;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreFactory;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.model.metadata.Metadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.repository.LocalRepository;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.repo.RepositoryFactory;
import ai.subut.kurjun.repo.RepositoryModule;
import ai.subut.kurjun.riparser.ReleaseIndexParserModule;
import ai.subut.kurjun.snap.SnapMetadataParserModule;
import ai.subut.kurjun.storage.factory.FileStoreFactory;
import ai.subut.kurjun.storage.factory.FileStoreModule;
import ai.subut.kurjun.subutai.SubutaiTemplateParserModule;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.common.protocol.TemplateKurjun;
import org.apache.commons.codec.binary.Hex;


public class TemplateManagerImpl implements TemplateManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );

    private static final Set<KurjunContext> CONTEXTS = new HashSet<>();

    private Injector injector;

    private final Set<URL> remoteRepoUrls = new HashSet<>();
    

    public void init()
    {

        injector = bootstrapDI();

        KurjunProperties properties = injector.getInstance( KurjunProperties.class );
        setContexts( properties );

        LOGGER.warn( "remoteRepoUrls = " + remoteRepoUrls );
    }


    public void dispose()
    {
    }


    @Override
    public TemplateKurjun getTemplate( String context, byte[] md5 ) throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );

        UnifiedRepository repo = getRepository( context );
        DefaultTemplate meta = ( DefaultTemplate ) repo.getPackageInfo( m );
        if ( meta != null )
        {
            return new TemplateKurjun( Hex.encodeHexString( meta.getMd5Sum() ), meta.getName(), meta.getVersion(),
                    meta.getArchitecture().name(), meta.getParent(), meta.getPackage() );
        }
        return null;
    }


    @Override
    public TemplateKurjun getTemplate( String context, String name, String version ) throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setName( name );
        m.setVersion( version );
        
        UnifiedRepository repo = getRepository( context );

        DefaultTemplate meta = ( DefaultTemplate ) repo.getPackageInfo( m );
        if ( meta != null )
        {
            return new TemplateKurjun( Hex.encodeHexString( meta.getMd5Sum() ), meta.getName(), meta.getVersion(),
                    meta.getArchitecture().name(), meta.getParent(), meta.getPackage() );
        }
        return null;
    }


    @Override
    public InputStream getTemplateData( String context, byte[] md5 ) throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );

        UnifiedRepository repo = getRepository( context );
        return repo.getPackageStream( m );
    }


    @Override
    public List<TemplateKurjun> list( String context ) throws IOException
    {
        UnifiedRepository repo = getRepository( context );
        List<SerializableMetadata> items = repo.listPackages();

        List<TemplateKurjun> result = new LinkedList<>();
        for ( SerializableMetadata item : items )
        {
            DefaultTemplate meta = ( DefaultTemplate ) item;
            result.add( new TemplateKurjun( Hex.encodeHexString( meta.getMd5Sum() ), meta.getName(), meta.getVersion(),
                    meta.getArchitecture().name(), meta.getParent(), meta.getPackage() ) );
        }
        return result;
    }


    @Override
    public byte[] upload( String context, InputStream inputStream ) throws IOException
    {
        LocalRepository repo = getLocalRepository( context );
        try
        {
            Metadata m = repo.put( inputStream, CompressionType.GZIP );
            return m.getMd5Sum();
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to put template", ex );
        }
        return null;
    }


    @Override
    public boolean delete( String context, byte[] md5 ) throws IOException
    {
        LocalRepository repo = getLocalRepository( context );
        try
        {
            repo.delete( md5 );
            return true;
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to delete template", ex );
            return false;
        }
    }


    @Override
    public void addRemoteRepository( URL url )
    {
        remoteRepoUrls.add( url );
        LOGGER.info( "Remote host url is added: {}", url );
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


    private LocalRepository getLocalRepository( String context ) throws IOException
    {
        KurjunContext c = getContext( context );
        if ( c != null )
        {
            RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
            return repositoryFactory.createLocalTemplate( c );
        }
        throw new IOException( "Invalid context" );
    }


    private UnifiedRepository getRepository( String context ) throws IOException
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        UnifiedRepository unifiedRepo = repositoryFactory.createUnifiedRepo();
        unifiedRepo.getRepositories().add( getLocalRepository( context ) );
        for ( URL url : remoteRepoUrls )
        {
            unifiedRepo.getRepositories().add( repositoryFactory.createNonLocalTemplate( url.toString(), null ) );
        }
        return unifiedRepo;
    }


    private static void setContexts( KurjunProperties properties )
    {
        // init template type contexts
        CONTEXTS.add( new KurjunContext( "public" ) );
        CONTEXTS.add( new KurjunContext( "trust" ) );

        for ( KurjunContext kc : CONTEXTS )
        {
            Properties kcp = properties.getContextProperties( kc );
            kcp.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
            kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE, PackageMetadataStoreFactory.FILE_DB );
        }

    }


    /**
     * Gets Kurjun context for templates repository type.
     * <p>
     *
     * @param context
     * @return
     */
    private KurjunContext getContext( String context )
    {
        Set<KurjunContext> set = CONTEXTS;
        for ( Iterator<KurjunContext> it = set.iterator(); it.hasNext(); )
        {
            KurjunContext c = it.next();
            if ( c.getName().equals( context ) )
            {
                return c;
            }
        }
        return null;
    }

}

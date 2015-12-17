package io.subutai.core.kurjun.impl.vapt;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Hex;

import com.google.inject.Injector;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.common.service.KurjunProperties;
import ai.subut.kurjun.index.PackagesIndexParserModule;
import ai.subut.kurjun.metadata.common.DefaultMetadata;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreFactory;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.model.index.ReleaseFile;
import ai.subut.kurjun.model.metadata.Architecture;
import ai.subut.kurjun.model.metadata.Metadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.repository.LocalRepository;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.repo.RepositoryFactory;
import ai.subut.kurjun.repo.RepositoryModule;
import ai.subut.kurjun.repo.service.PackageFilenameParser;
import ai.subut.kurjun.repo.service.PackagesIndexBuilder;
import ai.subut.kurjun.repo.util.AptIndexBuilderFactory;
import ai.subut.kurjun.repo.util.PackagesProviderFactory;
import ai.subut.kurjun.repo.util.ReleaseIndexBuilder;
import ai.subut.kurjun.riparser.ReleaseIndexParserModule;
import ai.subut.kurjun.snap.SnapMetadataParserModule;
import ai.subut.kurjun.storage.factory.FileStoreFactory;
import ai.subut.kurjun.storage.factory.FileStoreModule;
import ai.subut.kurjun.subutai.SubutaiTemplateParserModule;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.core.kurjun.api.vapt.AptManager;
import io.subutai.core.kurjun.impl.RepoUrl;
import io.subutai.core.kurjun.impl.RepoUrlStore;


public class AptManagerImpl implements AptManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( AptManagerImpl.class );

    private static final KurjunContext context = new KurjunContext( "my" );

    private Injector injector;

    private final LocalPeer localPeer;
    private Set<RepoUrl> remoteRepoUrls;
    private final RepoUrlStore repoUrlStore = new RepoUrlStore( Common.SUBUTAI_APP_DATA_PATH );


    public AptManagerImpl( LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    public void init()
    {
        injector = bootstrapDI();

        KurjunProperties properties = injector.getInstance( KurjunProperties.class );
        setContexts( properties );

        try
        {
            remoteRepoUrls = repoUrlStore.getRemoteAptUrls();
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to load remote apt repositories", ex );
        }
    }


    public void dispose()
    {
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


    @Override
    public String getRelease( String release, String component, String arch )
    {
        UnifiedRepository repo = getRepository();
        Optional<ReleaseFile> rel = repo.getDistributions().stream()
                .filter( r -> r.getCodename().equals( release ) ).findFirst();

        if ( rel.isPresent() )
        {
            AptIndexBuilderFactory indexBuilderFactory = injector.getInstance( AptIndexBuilderFactory.class );
            ReleaseIndexBuilder rib = indexBuilderFactory.createReleaseIndexBuilder( repo, context );
            return rib.build( rel.get(), repo.isKurjun() );
        }
        return null;
    }


    @Override
    public InputStream getPackagesIndex( String release, String component, String arch, String packagesIndex ) throws IllegalArgumentException
    {
        UnifiedRepository repo = getRepository();
        Optional<ReleaseFile> distr = repo.getDistributions().stream()
                .filter( r -> r.getCodename().equals( release ) ).findFirst();
        if ( !distr.isPresent() )
        {
            throw new IllegalArgumentException( "Release not found." );
        }
        if ( distr.get().getComponent( component ) == null )
        {
            throw new IllegalArgumentException( "Component not found." );
        }

        // arch string is like "binary-amd64"
        Architecture architecture = Architecture.getByValue( arch.substring( arch.indexOf( "-" ) + 1 ) );
        if ( architecture == null )
        {
            throw new IllegalArgumentException( "Architecture not supported." );
        }

        CompressionType compressionType = CompressionType.getCompressionType( packagesIndex );

        PackagesProviderFactory packagesProviderFactory = injector.getInstance( PackagesProviderFactory.class );
        AptIndexBuilderFactory indexBuilderFactory = injector.getInstance( AptIndexBuilderFactory.class );

        PackagesIndexBuilder packagesIndexBuilder = indexBuilderFactory.createPackagesIndexBuilder( context );
        try ( ByteArrayOutputStream os = new ByteArrayOutputStream() )
        {
            packagesIndexBuilder.buildIndex( packagesProviderFactory.create( repo, component, architecture ), os,
                                             compressionType );
            return new ByteArrayInputStream( os.toByteArray() );
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to build packages index", ex );
        }

        throw new IllegalArgumentException( "Failed to generate packages index." );
    }


    @Override
    public boolean isCompressionTypeSupported( String packagesIndex )
    {
        return CompressionType.getCompressionType( packagesIndex ) != CompressionType.NONE;
    }


    @Override
    public InputStream getPackageByFilename( String filename ) throws IllegalArgumentException
    {
        SerializableMetadata meta = getPackageInfoByFilename( filename );
        return ( meta != null ) ? getRepository().getPackageStream( meta ) : null;
    }


    @Override
    public String getSerializedPackageInfo( String filename ) throws IllegalArgumentException
    {
        SerializableMetadata meta = getPackageInfoByFilename( filename );
        return ( meta != null ) ? meta.serialize() : null;
    }


    @Override
    public String getSerializedPackageInfo( byte[] md5 ) throws IllegalArgumentException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );
        SerializableMetadata meta = getRepository().getPackageInfo( m );
        return ( meta != null ) ? meta.serialize() : null;
    }


    private SerializableMetadata getPackageInfoByFilename( String filename ) throws IllegalArgumentException
    {
        PackageFilenameParser filenameParser = injector.getInstance( PackageFilenameParser.class );
        String path = "/pool/" + filename;
        String packageName = filenameParser.getPackageFromFilename( path );
        String version = filenameParser.getVersionFromFilename( path );
        if ( packageName == null || version == null )
        {
            throw new IllegalArgumentException( "Invalid pool path" );
        }

        DefaultMetadata m = new DefaultMetadata();
        m.setName( packageName );
        m.setVersion( version );

        return getRepository().getPackageInfo( m );
    }


    @Override
    public URI upload( InputStream is )
    {
        Objects.requireNonNull( is, "The uploaded InputStream cannot be null" );
        try
        {
            Metadata meta = getLocalRepository().put( is );
            return new URI( null, null, "/info", "md5=" + Hex.encodeHexString( meta.getMd5Sum() ), null );
        }
        catch ( IOException | URISyntaxException ex )
        {
            LOGGER.error( "Failed to upload", ex );
            throw new IllegalArgumentException( "Failed to upload", ex );
        }
    }


    @Override
    public String getPackageInfo( byte[] md5, String name, String version )
    {
        if ( md5 == null && name == null && version == null )
        {
            return null;
        }

        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );
        m.setName( name );
        m.setVersion( version );

        SerializableMetadata meta = getRepository().getPackageInfo( m );
        if ( meta != null )
        {
            return meta.serialize();
        }
        return null;
    }


    @Override
    public InputStream getPackage( byte[] md5 )
    {
        if ( md5 == null )
        {
            return null;
        }

        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );

        UnifiedRepository repo = getRepository();
        SerializableMetadata meta = repo.getPackageInfo( m );
        if ( meta != null )
        {
            return repo.getPackageStream( meta );
        }
        return null;
    }


    @Override
    public void addRemoteRepository( URL url, String token )
    {
        try
        {
            if ( url != null && !url.getHost().equals( localPeer.getManagementHost().getExternalIp() ) )
            {
                repoUrlStore.addRemoteAptUrl( new RepoUrl( url, token ) );
                remoteRepoUrls = repoUrlStore.getRemoteAptUrls();
            }
        }
        catch ( HostNotFoundException | IOException ex )
        {
            LOGGER.error( "Failed to add remote apt repo: {}", url, ex );
        }
    }


    @Override
    public void removeRemoteRepository( URL url )
    {
        Objects.requireNonNull( url, "URL to remove" );
        try
        {
            RepoUrl r = repoUrlStore.removeRemoteAptUrl( new RepoUrl( url, null ) );
            if ( r != null )
            {
                remoteRepoUrls = repoUrlStore.getRemoteAptUrls();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to remove remote apr repo: {}", url, ex );
        }
    }


    private UnifiedRepository getRepository()
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );

        UnifiedRepository unifiedRepo = repositoryFactory.createUnifiedRepo();
        unifiedRepo.getRepositories().add( repositoryFactory.createLocalApt( context ) );

        for ( RepoUrl remote : remoteRepoUrls )
        {
            unifiedRepo.getRepositories().add( repositoryFactory.createNonLocalApt( remote.getUrl() ) );
        }
        return unifiedRepo;
    }


    private LocalRepository getLocalRepository()
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        return repositoryFactory.createLocalApt( context );
    }


    private void setContexts( KurjunProperties properties )
    {
        // init apt type context
        Properties kcp = properties.getContextProperties( context );
        kcp.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
        kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE, PackageMetadataStoreFactory.FILE_DB );

    }

}


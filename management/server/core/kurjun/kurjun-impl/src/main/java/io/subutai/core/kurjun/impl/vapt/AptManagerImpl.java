package io.subutai.core.kurjun.impl.vapt;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.common.service.KurjunProperties;
import ai.subut.kurjun.common.utils.InetUtils;
import ai.subut.kurjun.index.PackagesIndexParserModule;
import ai.subut.kurjun.metadata.common.DefaultMetadata;
import ai.subut.kurjun.metadata.common.apt.DefaultPackageMetadata;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreFactory;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.model.index.ReleaseFile;
import ai.subut.kurjun.model.metadata.Architecture;
import ai.subut.kurjun.model.metadata.Metadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.repository.LocalRepository;
import ai.subut.kurjun.model.repository.Repository;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.quota.DataUnit;
import ai.subut.kurjun.quota.QuotaInfoStore;
import ai.subut.kurjun.quota.disk.DiskQuota;
import ai.subut.kurjun.quota.transfer.TransferQuota;
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
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.AptPackage;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.vapt.AptManager;
import io.subutai.core.kurjun.impl.TrustedWebClientFactoryModule;
import io.subutai.core.kurjun.impl.model.RepoUrl;
import io.subutai.core.kurjun.impl.store.RepoUrlStore;


public class AptManagerImpl implements AptManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( AptManagerImpl.class );

    private static final String VAPT_PATH = "/deb";

    private static final KurjunContext APT_CONTEXT = new KurjunContext( "vapt" );
    private static final String DEFAULT_APT_REPO = "vapt";
    private Injector injector;

    private final LocalPeer localPeer;

    private Set<RepoUrl> remoteRepoUrls;

    private RepositoryFactory repositoryFactory;
    private UnifiedRepository unifiedRepository;
    private LocalRepository localAptRepository;

    private static Map<String, String> debsInSync = new ConcurrentHashMap();

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

        initRepoUrls();

        _local();

        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    _remote();
                }
                catch ( MalformedURLException e )
                {
                    LOGGER.error("Invalid url format exception. " + e.getMessage());
                }
            }
        } ).start();
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
        bootstrap.addModule( new TrustedWebClientFactoryModule() );
        //        bootstrap.addModule( new SecurityModule() );

        bootstrap.boot();

        return bootstrap.getInjector();
    }


    private void _local()
    {
        this.repositoryFactory = injector.getInstance( RepositoryFactory.class );

        this.localAptRepository = this.repositoryFactory.createLocalApt( new KurjunContext( DEFAULT_APT_REPO ) );
    }


    private void _remote() throws MalformedURLException
    {

        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        this.unifiedRepository = repositoryFactory.createUnifiedRepo();

        for ( String s : SystemSettings.getGlobalKurjunUrls() )
        {
            this.unifiedRepository.getRepositories().add( repositoryFactory.createNonLocalApt( new URL( s ), "all" ) );
        }
    }


    @Override
    public String getRelease( String release, String component, String arch )
    {

        Optional<ReleaseFile> rel =
                unifiedRepository.getDistributions().stream().filter( r -> r.getCodename().equals( release ) )
                                 .findFirst();

        if ( rel.isPresent() )
        {
            AptIndexBuilderFactory indexBuilderFactory = injector.getInstance( AptIndexBuilderFactory.class );
            ReleaseIndexBuilder rib = indexBuilderFactory.createReleaseIndexBuilder( unifiedRepository, APT_CONTEXT );
            return rib.build( rel.get(), unifiedRepository.isKurjun() );
        }
        return null;
    }


    @Override
    public InputStream getPackagesIndex( String release, String component, String arch, String packagesIndex )
            throws IllegalArgumentException
    {

        Optional<ReleaseFile> distr =
                unifiedRepository.getDistributions().stream().filter( r -> r.getCodename().equals( release ) )
                                 .findFirst();
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

        PackagesIndexBuilder packagesIndexBuilder = indexBuilderFactory.createPackagesIndexBuilder( APT_CONTEXT );
        try ( ByteArrayOutputStream os = new ByteArrayOutputStream() )
        {
            packagesIndexBuilder
                    .buildIndex( packagesProviderFactory.create( unifiedRepository, component, architecture ), os,
                            compressionType );
            InputStream is = new ByteArrayInputStream( os.toByteArray() );
//            upload( is );
            return is;
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
    public InputStream getPackageByFilename( String filename, Repository.PackageProgressListener progressListener  )
            throws IllegalArgumentException
    {
        SerializableMetadata meta = getPackageInfoByFilename( filename );
        while ( debsInSync.get( filename ) != null )
        {
            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
        debsInSync.put( filename, filename );

        if ( meta != null )
        {
            debsInSync.remove( filename );
            InputStream is = unifiedRepository.getPackageStream( meta, progressListener );
//            upload( is );
            return is;
        }
        else
        {
            debsInSync.remove( filename );
            return null;
        }
    }


    @Override
    public String getSerializedPackageInfoByName( String filename ) throws IllegalArgumentException
    {
        SerializableMetadata meta = getPackageInfoByFilename( filename );
        return ( meta != null ) ? meta.serialize() : null;
    }


    @Override
    public String getSerializedPackageInfoByMd5( String md5 ) throws IllegalArgumentException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );
        SerializableMetadata meta = unifiedRepository.getPackageInfo( m );
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

        return unifiedRepository.getPackageInfo( m );
    }


    @Override
    public URI upload( InputStream is )
    {
        Objects.requireNonNull( is, "The uploaded InputStream cannot be null" );
        try
        {
            Metadata meta = getLocalRepository().put( is );
            return new URI( null, null, "/info", "md5=" +  meta.getMd5Sum(), null );
        }
        catch ( IOException | URISyntaxException ex )
        {
            LOGGER.error( "Failed to upload", ex );
            throw new IllegalArgumentException( ex.getMessage(), ex );
        }
    }


    @Override
    public List<AptPackage> list()
    {
        List<SerializableMetadata> list = unifiedRepository.listPackages();

        List<AptPackage> deflist = list.stream().map( t -> convertToAptPackage( ( DefaultPackageMetadata ) t ) )
                                       .collect( Collectors.toList() );
        return deflist;
    }


    @Override
    public boolean delete( String md5 ) throws IOException
    {
        LocalRepository repo = getLocalRepository();
        try
        {
            return repo.delete( md5 );
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to delete apt package", ex );
            throw ex;
        }
    }


    private AptPackage convertToAptPackage( DefaultPackageMetadata meta )
    {
        return new AptPackage( meta.getMd5Sum(), meta.getName(), meta.getVersion(),
                meta.getSource(), meta.getMaintainer(), meta.getArchitecture().name(), meta.getInstalledSize(),
                meta.getDescription() );
    }


    @Override
    public String getPackageInfo( String md5, String name, String version )
    {
        if ( md5 == null && name == null && version == null )
        {
            return null;
        }

        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );
        m.setName( name );
        m.setVersion( version );

        SerializableMetadata meta = unifiedRepository.getPackageInfo( m );
        if ( meta != null )
        {
            return meta.serialize();
        }
        return null;
    }


    @Override
    public InputStream getPackage( String md5, Repository.PackageProgressListener progressListener  )
    {
        if ( md5 == null )
        {
            return null;
        }

        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );


        SerializableMetadata meta = unifiedRepository.getPackageInfo( m );
        if ( meta != null )
        {
            InputStream is = unifiedRepository.getPackageStream( meta, progressListener );
//            upload( is );
            return is;
        }
        return null;
    }


    @Override
    public void addRemoteRepository( URL url, String token )
    {
        if ( isEmbedded() )
        {
            return;
        }
        try
        {
            if ( url != null && !url.getHost().equals( getExternalIp() ) )
            {
                repoUrlStore.addRemoteAptUrl( new RepoUrl( url, token ) );
                remoteRepoUrls = repoUrlStore.getRemoteAptUrls();
            }
        }
        catch ( IOException ex )
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


    private String getExternalIp()
    {
        try
        {
            if ( isEmbedded() )
            {
                List<InetAddress> ips = InetUtils.getLocalIPAddresses();
                return ips.get( 0 ).getHostAddress();
            }
            else
            {
                return localPeer.getExternalIp();
            }
        }
        catch ( SocketException | IndexOutOfBoundsException ex )
        {
            LOGGER.error( "Cannot get external ip. Returning null.", ex );
            return null;
        }
    }


    @Override
    public Long getDiskQuota( String context )
    {
        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            DiskQuota diskQuota = quotaInfoStore.getDiskQuota( AptManagerImpl.APT_CONTEXT );
            if ( diskQuota != null )
            {
                return diskQuota.getThreshold() * diskQuota.getUnit().toBytes() / DataUnit.MB.toBytes();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to get disk quota", ex );
        }
        return null;
    }


    @Override
    public boolean setDiskQuota( long size, String context )
    {
        DiskQuota diskQuota = new DiskQuota( size, DataUnit.MB );
        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            quotaInfoStore.saveDiskQuota( diskQuota, AptManagerImpl.APT_CONTEXT );
            return true;
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to save disk quota", ex );
            return false;
        }
    }


    @Override
    public KurjunTransferQuota getTransferQuota( String context )
    {
        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            TransferQuota q = quotaInfoStore.getTransferQuota( AptManagerImpl.APT_CONTEXT );
            if ( q != null )
            {
                return new KurjunTransferQuota( q.getThreshold(), q.getTime(), q.getTimeUnit() );
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to get transfer quota", ex );
        }
        return null;
    }


    @Override
    public boolean setTransferQuota( KurjunTransferQuota quota, String context )
    {
        TransferQuota transferQuota = new TransferQuota();
        transferQuota.setThreshold( quota.getThreshold() );
        transferQuota.setUnit( DataUnit.MB );
        transferQuota.setTime( quota.getTimeFrame() );
        transferQuota.setTimeUnit( quota.getTimeUnit() );

        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            quotaInfoStore.saveTransferQuota( transferQuota, AptManagerImpl.APT_CONTEXT );
            return true;
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to save transfer quota", ex );
            return false;
        }
    }


    private LocalRepository getLocalRepository()
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        return repositoryFactory.createLocalApt( APT_CONTEXT );
    }


    private void setContexts( KurjunProperties properties )
    {
        // init apt type context
        Properties kcp = properties.getContextProperties( APT_CONTEXT );
        kcp.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
        kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE, PackageMetadataStoreFactory.FILE_DB );
    }


    private void initRepoUrls()
    {
        try
        {
            // Load remote repo urls from store
            remoteRepoUrls = repoUrlStore.getRemoteAptUrls();
        }
        catch ( IOException e )
        {
            LOGGER.error( "Failed to load remote apt repositories", e );
        }
    }


    private boolean isEmbedded()
    {
        return localPeer == null;
    }


    private List<RepoUrl> getGlobalKurjunUrls()
    {
        if ( isEmbedded() )
        {
            return Collections.emptyList();
        }

        try
        {
            List<RepoUrl> list = new ArrayList<>();
            for ( String url : SystemSettings.getGlobalKurjunUrls() )
            {
                String aptUrl = url + VAPT_PATH;
                list.add( new RepoUrl( new URL( aptUrl ), null ) );
            }
            return list;
        }
        catch ( MalformedURLException e )
        {
            throw new IllegalArgumentException( "Invalid global kurjun url", e );
        }
    }
}

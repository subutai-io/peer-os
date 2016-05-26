package io.subutai.core.kurjun.impl.raw;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.helpers.IOUtils;

import com.google.inject.Injector;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.index.PackagesIndexParserModule;
import ai.subut.kurjun.metadata.common.DefaultMetadata;
import ai.subut.kurjun.metadata.common.raw.RawMetadata;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.model.metadata.Metadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.repository.Repository;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.repo.LocalRawRepository;
import ai.subut.kurjun.repo.RepositoryFactory;
import ai.subut.kurjun.repo.RepositoryModule;
import ai.subut.kurjun.riparser.ReleaseIndexParserModule;
import ai.subut.kurjun.snap.SnapMetadataParserModule;
import ai.subut.kurjun.storage.factory.FileStoreModule;
import ai.subut.kurjun.subutai.SubutaiTemplateParserModule;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.kurjun.api.raw.RawManager;
import io.subutai.core.kurjun.impl.TemplateManagerImpl;
import io.subutai.core.kurjun.impl.TrustedWebClientFactoryModule;
import io.subutai.core.kurjun.impl.model.RepoUrl;
import io.subutai.core.kurjun.impl.store.RepoUrlStore;


public class RawManagerImpl implements RawManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );

    private static final String DEFAULT_RAW_REPO_NAME = "raw";

    private static final String RAW_PATH = "/file";

    private RepositoryFactory repositoryFactory;
    private UnifiedRepository unifiedRepository;
    private LocalRawRepository localPublicRawRepository;


    private Set<RepoUrl> remoteRepoUrls = new HashSet<>();

    private final RepoUrlStore repoUrlStore = new RepoUrlStore( Common.SUBUTAI_APP_DATA_PATH );

    private Injector injector;

    private static Map<String, String> rawInSync = new ConcurrentHashMap();


    public RawManagerImpl()
    {
        injector = bootstrapDI();

        _local();

        _unified();
    }


    public void init()
    {
    }


    private void _local()
    {
        this.repositoryFactory = injector.getInstance( RepositoryFactory.class );

        this.localPublicRawRepository =
                this.repositoryFactory.createLocalRaw( new KurjunContext( DEFAULT_RAW_REPO_NAME ) );
    }


    private void _unified()
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );

        this.unifiedRepository = repositoryFactory.createUnifiedRepo();

        for ( String s : SystemSettings.getGlobalKurjunUrls() )
        {
            this.unifiedRepository.getRepositories().add( repositoryFactory.createNonLocalRaw( s, null, "all" ) );
        }
        this.unifiedRepository.getRepositories().add( this.localPublicRawRepository );
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


        bootstrap.boot();

        return bootstrap.getInjector();
    }


    @Override
    public String md5()
    {
        return localPublicRawRepository.md5();
    }


    @Override
    public RawMetadata getInfo( final String repository, final String md5 )
    {
        RawMetadata rawMetadata = new RawMetadata();
        rawMetadata.setFingerprint( repository );
        rawMetadata.setMd5Sum( md5 );

        return ( RawMetadata ) unifiedRepository.getPackageInfo( rawMetadata );
    }


    @Override
    public RawMetadata getInfo( final String repository, final String name, final String version, final String md5 )
    {
        RawMetadata rawMetadata = new RawMetadata();
        rawMetadata.setFingerprint( repository );
        rawMetadata.setMd5Sum( md5 );
        rawMetadata.setName( name );

        return ( RawMetadata ) unifiedRepository.getPackageInfo( rawMetadata );
    }


    @Override
    public Object getInfo( final Object metadata )
    {
        return null;
    }


    //    @Override
    public SerializableMetadata getInfo( final SerializableMetadata metadata )
    {
        return unifiedRepository.getPackageInfo( metadata );
    }


    @Override
    public boolean delete( String repository, final String md5 )
    {
        DefaultMetadata defaultMetadata = new DefaultMetadata();
        defaultMetadata.setFingerprint( repository );
        defaultMetadata.setMd5sum( md5 );
        try
        {
            return localPublicRawRepository.delete( defaultMetadata.getId(), md5 );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public RawMetadata getInfo( final String md5 )
    {
        DefaultMetadata metadata = new DefaultMetadata();
        metadata.setMd5sum( md5 );
        return ( RawMetadata ) unifiedRepository.getPackageInfo( metadata );
    }


    @Override
    public RawMetadata put( final File file )
    {
        Metadata metadata = null;
        try
        {
            metadata = localPublicRawRepository.put( file, CompressionType.NONE, DEFAULT_RAW_REPO_NAME );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return ( RawMetadata ) metadata;
    }


    @Override
    @Deprecated
    public RawMetadata put( final File file, final String repository )
    {
        Metadata metadata = null;
        try
        {
            metadata = localPublicRawRepository.put( new FileInputStream( file ), CompressionType.NONE, repository );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return ( RawMetadata ) metadata;
    }


    @Override
    public RawMetadata put( final File file, final String filename, final String repository )
    {

        Metadata metadata = null;
        try
        {

            LocalRawRepository localRawRepository = getLocalPublicRawRepository( new KurjunContext( repository ) );
            metadata = localRawRepository.put( file, filename, repository );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return ( RawMetadata ) metadata;
    }


    public LocalRawRepository getLocalPublicRawRepository( KurjunContext context )
    {
        return repositoryFactory.createLocalRaw( context );
    }


    @Override
    public InputStream getFile( final String repository, final String md5,
                                Repository.PackageProgressListener progressListener  ) throws IOException
    {
        RawMetadata rawMetadata = new RawMetadata();
        rawMetadata.setMd5Sum( md5 );
        rawMetadata.setFingerprint( repository );

        while ( rawInSync.get( md5 ) != null )
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

        rawInSync.put( md5, md5 );

        InputStream inputStream = unifiedRepository.getPackageStream( rawMetadata, progressListener );

        rawInSync.remove( md5 );

        return inputStream;
    }


    @Override
    public InputStream getFile( final RawMetadata rawMetadata, final Repository.PackageProgressListener
            progressListener )
            throws IOException
    {
        InputStream inputStream = getFile( rawMetadata.getFingerprint(), rawMetadata.getMd5Sum(), progressListener );
        File tempFile = Files.createTempFile( null, null ).toFile();
        IOUtils.transferTo(inputStream, tempFile);

        put( tempFile, rawMetadata.getName(), rawMetadata.getFingerprint());
        return inputStream;
    }


    @Override
    public List<SerializableMetadata> list( String repository )
    {
        return unifiedRepository.listPackages();
    }


    @Override
    public boolean delete( final String md5 ) throws IOException
    {
        DefaultMetadata defaultMetadata = new DefaultMetadata();

        defaultMetadata.setMd5sum( md5 );
        return localPublicRawRepository.delete( md5 );
    }
}

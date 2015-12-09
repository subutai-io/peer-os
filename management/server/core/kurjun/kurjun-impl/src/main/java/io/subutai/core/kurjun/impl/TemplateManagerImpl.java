package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Hex;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.common.service.KurjunProperties;
import ai.subut.kurjun.common.utils.InetUtils;
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
import com.google.common.base.Strings;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.settings.Common;
import io.subutai.core.kurjun.api.TemplateManager;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


public class TemplateManagerImpl implements TemplateManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );

    private static final Set<KurjunContext> CONTEXTS = new HashSet<>();

    private List<String> globalKurjunUrls = new ArrayList<>();

    private Injector injector;

    private Set<RepoUrl> remoteRepoUrls = new HashSet<>();

    private Set<RepoUrl> globalRepoUrls = new HashSet<>();

    private final LocalPeer localPeer;

    private final RepoUrlStore repoUrlStore = new RepoUrlStore( Common.SUBUTAI_APP_DATA_PATH );


    public TemplateManagerImpl( LocalPeer localPeer, String globalKurjunUrl )
    {
        this.localPeer = localPeer;
        parseGlobalKurjunUrls( globalKurjunUrl );

    }


    public void init()
    {
        injector = bootstrapDI();

        try
        {
            // Load remote repo urls from store
            remoteRepoUrls = repoUrlStore.getRemoteTemplateUrls();

            // Refresh global urls
            if ( !globalKurjunUrls.isEmpty() )
            {
                repoUrlStore.removeAllGlobalTemplateUrl();
                for ( String url : globalKurjunUrls )
                {
                    repoUrlStore.addGlobalTemplateUrl( new RepoUrl( new URL( url ), false ) );
                }
            }

            // Load global repo urls from store
            globalRepoUrls = repoUrlStore.getGlobalTemplateUrls();
        }
        catch ( IOException e )
        {
            LOGGER.error( "Failed to get remote repo urls", e );
        }
        
        logAllUrlsInUse();

        KurjunProperties properties = injector.getInstance( KurjunProperties.class );
        setContexts( properties );
    }


    public void dispose()
    {
    }


    @Override
    public TemplateKurjun getTemplate( String context, byte[] md5, boolean isKurjunClient ) throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );

        UnifiedRepository repo = getRepository( context, isKurjunClient );
        DefaultTemplate meta = ( DefaultTemplate ) repo.getPackageInfo( m );
        if ( meta != null )
        {
            return new TemplateKurjun( Hex.encodeHexString( meta.getMd5Sum() ), meta.getName(), meta.getVersion(),
                    meta.getArchitecture().name(), meta.getParent(), meta.getPackage() );
        }
        return null;
    }


    @Override
    public TemplateKurjun getTemplate( String context, String name, String version, boolean isKurjunClient ) throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setName( name );
        m.setVersion( version );

        UnifiedRepository repo = getRepository( context, isKurjunClient );

        DefaultTemplate meta = ( DefaultTemplate ) repo.getPackageInfo( m );
        if ( meta != null )
        {
            return new TemplateKurjun( Hex.encodeHexString( meta.getMd5Sum() ), meta.getName(), meta.getVersion(),
                    meta.getArchitecture().name(), meta.getParent(), meta.getPackage() );
        }
        return null;
    }


    @Override
    public TemplateKurjun getTemplate( final String name )
    {
        try
        {
            return getTemplate( PUBLIC_REPO, name, null, false );
        }
        catch ( IOException e )
        {
            LOGGER.error( "Error in getTemplate(name)", e );

            return null;
        }
    }


    @Override
    public List<URL> getRemoteRepoUrls()
    {
        List<URL> urls = new ArrayList<>();
        try
        {
            for ( RepoUrl r : repoUrlStore.getRemoteTemplateUrls() )
            {
                urls.add( r.getUrl() );
            }
            
            for ( RepoUrl r : repoUrlStore.getGlobalTemplateUrls() )
            {
                urls.add( r.getUrl() );
            }
        }
        catch ( IOException e )
        {
            LOGGER.error( "", e );
        }
        return urls;
    }


    @Override
    public InputStream getTemplateData( String context, byte[] md5, boolean isKurjunClient ) throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setMd5sum( md5 );

        UnifiedRepository repo = getRepository( context, isKurjunClient );
        return repo.getPackageStream( m );
    }


    @Override
    public List<TemplateKurjun> list( String context, boolean isKurjunClient ) throws IOException
    {
        UnifiedRepository repo = getRepository( context, isKurjunClient );
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
    public List<TemplateKurjun> list()
    {
        try
        {
            return list( PUBLIC_REPO, false );
        }
        catch ( IOException e )
        {
            LOGGER.error( "Error in list", e );
            return Lists.newArrayList();
        }
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
        addRemoteRepository( url, true );
    }


    @Override
    public void addRemoteRepository( URL url, boolean useToken )
    {
        try
        {
            if ( url != null && !url.getHost().equals( getExternalIp() ) )
            {
                repoUrlStore.addRemoteTemplateUrl( new RepoUrl( url, useToken ) );
                remoteRepoUrls = repoUrlStore.getRemoteTemplateUrls();
                LOGGER.info( "Remote template host url is added: {}", url );
            }
            else
            {
                LOGGER.error( "Failed to add remote host url: {}", url );
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to add remote host url: {}", url, ex );
        }
    }


    @Override
    public void removeRemoteRepository( URL url )
    {
        if ( url != null )
        {
            try
            {
                RepoUrl r = repoUrlStore.removeRemoteTemplateUrl( url );
                if ( r != null )
                {
                    LOGGER.info( "Remote template host url is removed: {}", url );
                }
                else
                {
                    LOGGER.warn( "Failed to remove remote host url: {}. Either it does not exist or it is a global url", url );
                }
                remoteRepoUrls = repoUrlStore.getRemoteTemplateUrls();
            }
            catch ( IOException e )
            {
                LOGGER.error( "Failed to remove remote host url: {}", url, e );
            }
        }
    }


    private String getExternalIp()
    {
        try
        {
            if ( localPeer != null )
            {
                return localPeer.getManagementHost().getExternalIp();
            }
            else
            {
                List<InetAddress> ips = InetUtils.getLocalIPAddresses();
                return ips.get( 0 ).getHostAddress();
            }
        }
        catch ( SocketException | IndexOutOfBoundsException | HostNotFoundException ex )
        {
            LOGGER.error( "Cannot get external ip. Returning null.", ex );
            return null;
        }
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


    private UnifiedRepository getRepository( String context, boolean isKurjunClient ) throws IOException
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        UnifiedRepository unifiedRepo = repositoryFactory.createUnifiedRepo();
        unifiedRepo.getRepositories().add( getLocalRepository( context ) );

        if ( ! isKurjunClient )
        {
            for ( RepoUrl repoUrl : remoteRepoUrls )
            {
                unifiedRepo.getRepositories().add( repositoryFactory.createNonLocalTemplate( repoUrl.getUrl().toString(), null, repoUrl.isUseToken() ) );
            }

            for ( RepoUrl repoUrl : globalRepoUrls )
            {
                unifiedRepo.getSecondaryRepositories().add( repositoryFactory.createNonLocalTemplate( repoUrl.getUrl().toString(), null, repoUrl.isUseToken() ) );
            }
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
            kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE,
                    PackageMetadataStoreFactory.FILE_DB );
        }
    }


    /**
     * Gets Kurjun context for templates repository type.
     */
    private KurjunContext getContext( String context )
    {
        Set<KurjunContext> set = CONTEXTS;
        for ( KurjunContext c : set )
        {
            if ( c.getName().equals( context ) )
            {
                return c;
            }
        }
        return null;
    }


    private void parseGlobalKurjunUrls( String globalKurjunUrl )
    {
        if ( !Strings.isNullOrEmpty( globalKurjunUrl ) )
        {
            String urls[] = globalKurjunUrl.split( "," );

            for ( int x = 0; x < urls.length; x++ )
            {
                urls[x] = urls[x].trim();
                globalKurjunUrls.add( urls[x] );
            }
        }
    }


    private void logAllUrlsInUse()
    {
        LOGGER.info( "Remote template urls:" );
        for ( RepoUrl r : remoteRepoUrls )
        {
            LOGGER.info( r.toString() );
        }

        for ( RepoUrl r : globalRepoUrls )
        {
            LOGGER.info( r.toString() );
        }
    }
}

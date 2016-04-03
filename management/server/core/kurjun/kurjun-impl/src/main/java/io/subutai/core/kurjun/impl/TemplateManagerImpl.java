package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.PermitAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Hex;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.cfparser.ControlFileParserModule;
import ai.subut.kurjun.common.KurjunBootstrap;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.common.utils.InetUtils;
import ai.subut.kurjun.index.PackagesIndexParserModule;
import ai.subut.kurjun.metadata.common.DefaultMetadata;
import ai.subut.kurjun.metadata.common.subutai.DefaultTemplate;
import ai.subut.kurjun.metadata.common.subutai.TemplateId;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.metadata.template.SubutaiTemplateMetadata;
import ai.subut.kurjun.model.repository.RemoteRepository;
import ai.subut.kurjun.model.repository.Repository;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.quota.DataUnit;
import ai.subut.kurjun.quota.QuotaInfoStore;
import ai.subut.kurjun.quota.QuotaManagementModule;
import ai.subut.kurjun.quota.QuotaManagerFactory;
import ai.subut.kurjun.quota.disk.DiskQuota;
import ai.subut.kurjun.quota.transfer.TransferQuota;
import ai.subut.kurjun.quota.transfer.TransferQuotaManager;
import ai.subut.kurjun.repo.LocalTemplateRepository;
import ai.subut.kurjun.repo.RepositoryFactory;
import ai.subut.kurjun.repo.RepositoryModule;
import ai.subut.kurjun.riparser.ReleaseIndexParserModule;
import ai.subut.kurjun.snap.SnapMetadataParserModule;
import ai.subut.kurjun.storage.factory.FileStoreModule;
import ai.subut.kurjun.subutai.SubutaiTemplateParserModule;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.SharedTemplate;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.kurjun.api.template.TemplateRepository;
import io.subutai.core.kurjun.impl.model.RepoUrl;
import io.subutai.core.kurjun.impl.store.RepoUrlStore;


@PermitAll
public class TemplateManagerImpl implements TemplateManager
{


    //
    private LocalTemplateRepository localTemplateRepository;

    private UnifiedRepository unifiedRepository;
    //
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );

    private static final String TEMPLATE_PATH = "/template";

    private Injector injector;

    private final LocalPeer localPeer;

    private final RepoUrlStore repoUrlStore = new RepoUrlStore( Common.SUBUTAI_APP_DATA_PATH );


    public TemplateManagerImpl( LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    public void init()
    {
        injector = bootstrapDI();

        _local();

        _remote();
    }


    private void _local()
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );

        this.localTemplateRepository =
                ( LocalTemplateRepository ) repositoryFactory.createLocalTemplate( new KurjunContext( "public" ) );
    }


    private void _remote()
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        this.unifiedRepository = repositoryFactory.createUnifiedRepo();

        for ( String s : SystemSettings.getGlobalKurjunUrls() )
        {
            this.unifiedRepository.getRepositories()
                                  .add( repositoryFactory.createNonLocalTemplate( s, null, "public", null, "all" ) );
        }
        this.unifiedRepository.getRepositories().add( this.localTemplateRepository );
    }


    public void dispose()
    {

    }


    @Override
    public TemplateKurjun getTemplate( String repository, byte[] md5, String templateOwner, boolean isKurjunClient )
            throws IOException
    {

        DefaultTemplate m = new DefaultTemplate();
        m.setId( templateOwner, md5 );


        SubutaiTemplateMetadata meta = ( SubutaiTemplateMetadata ) unifiedRepository.getPackageInfo( m );

        if ( meta != null )
        {
            return convertToSubutaiTemplate( meta );
        }

        return null;
    }


    @Override
    public TemplateKurjun getTemplate( String repository, String name, String version, boolean isKurjunClient )
            throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setName( name );
        m.setVersion( version );
        m.setFingerprint( repository );

        SubutaiTemplateMetadata meta = ( SubutaiTemplateMetadata ) unifiedRepository.getPackageInfo( m );
        if ( meta != null )
        {
            return convertToSubutaiTemplate( meta );
        }
        return null;
    }


    @Override
    public TemplateKurjun getTemplate( final String name )
    {
        try
        {
            return getTemplate( TemplateRepository.PUBLIC, name, null, false );
        }
        catch ( IOException e )
        {
            LOGGER.error( "Error in getTemplate(name)", e );

            return null;
        }
    }


    @Override
    public InputStream getTemplateData( String repository, byte[] md5, String templateOwner, boolean isKurjunClient )
            throws IOException
    {


        DefaultTemplate m = new DefaultTemplate();
        m.setId( templateOwner, md5 );

        InputStream is = unifiedRepository.getPackageStream( m );

        if ( is != null )
        {
            QuotaManagerFactory quotaManagerFactory = injector.getInstance( QuotaManagerFactory.class );
            TransferQuotaManager qm = quotaManagerFactory.createTransferQuotaManager( new KurjunContext( repository ) );
            return qm.createManagedStream( is );
        }
        return null;
    }


    @Override
    public List<TemplateKurjun> list( String repository, boolean isKurjunClient ) throws IOException
    {
        List<SerializableMetadata> metadatas = unifiedRepository.listPackages();

        List<TemplateKurjun> result = new LinkedList<>();

        for ( SerializableMetadata metadata : metadatas )
        {
            DefaultTemplate templateMeta = ( DefaultTemplate ) metadata;

            result.add( convertToSubutaiTemplate( templateMeta ) );
        }

        return result;
    }


    @Override
    public List<TemplateKurjun> list()
    {
        try
        {
            return list( TemplateRepository.PUBLIC, false );
        }
        catch ( IOException e )
        {
            LOGGER.error( "Error in list", e );
            return Lists.newArrayList();
        }
    }


    @Override
    public String upload( final String repository, final InputStream inputStream ) throws IOException
    {

        DefaultTemplate defaultTemplate =
                ( DefaultTemplate ) localTemplateRepository.put( inputStream, CompressionType.GZIP, repository );

        if ( defaultTemplate != null )
        {
            return defaultTemplate.getId().toString();
        }
        return null;
    }


    @Override
    public boolean delete( String repository, String templateOwner, byte[] md5 ) throws IOException
    {

        try
        {
            TemplateId tid = new TemplateId( templateOwner, Hex.encodeHexString( md5 ) );
            return localTemplateRepository.delete( tid.get(), md5 );
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to delete template", ex );
            throw ex;
        }
    }


    @Override
    public void addRemoteRepository( URL url, String token )
    {
        try
        {
            if ( url != null && !url.getHost().equals( getExternalIp() ) )
            {
                String urlStr = url.toExternalForm();
                String u = urlStr.endsWith( "/" ) ? urlStr.replaceAll( "/+$", "" ) : urlStr;
                String templateUrl = u + TEMPLATE_PATH;
                repoUrlStore.addRemoteTemplateUrl( new RepoUrl( new URL( templateUrl ), token ) );

                LOGGER.info( "Remote template host url is added: {}", templateUrl );
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
                RepoUrl r = repoUrlStore.removeRemoteTemplateUrl( new RepoUrl( url, null ) );
                if ( r != null )
                {
                    LOGGER.info( "Remote template host url is removed: {}", url );
                }
                else
                {
                    LOGGER.warn( "Failed to remove remote host url: {}. Either it does not exist or it is a global url",
                            url );
                }
            }
            catch ( IOException e )
            {
                LOGGER.error( "Failed to remove remote host url: {}", url, e );
            }
        }
    }


    @Override
    public Long getDiskQuota( String repository )
    {
        KurjunContext c = new KurjunContext( repository );
        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            DiskQuota diskQuota = quotaInfoStore.getDiskQuota( c );
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
    public boolean setDiskQuota( long size, String repository )
    {
        KurjunContext kurjunContext = new KurjunContext( repository );

        DiskQuota diskQuota = new DiskQuota( size, DataUnit.MB );
        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            quotaInfoStore.saveDiskQuota( diskQuota, kurjunContext );
            return true;
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to save disk quota", ex );
            return false;
        }
    }


    @Override
    public KurjunTransferQuota getTransferQuota( String repository )
    {
        KurjunContext c = new KurjunContext( repository );
        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            TransferQuota q = quotaInfoStore.getTransferQuota( c );
            if ( q != null )
            {
                return new KurjunTransferQuota( q.getThreshold(), q.getTime(), q.getTimeUnit() );
            }
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to get disk quota", ex );
        }
        return null;
    }


    @Override
    public boolean setTransferQuota( KurjunTransferQuota quota, String repository )
    {
        KurjunContext kurjunContext = new KurjunContext( repository );

        TransferQuota transferQuota = new TransferQuota();
        transferQuota.setThreshold( quota.getThreshold() );
        transferQuota.setUnit( DataUnit.MB );
        transferQuota.setTime( quota.getTimeFrame() );
        transferQuota.setTimeUnit( quota.getTimeUnit() );

        QuotaInfoStore quotaInfoStore = injector.getInstance( QuotaInfoStore.class );
        try
        {
            quotaInfoStore.saveTransferQuota( transferQuota, kurjunContext );
            return true;
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to save transfer quota", ex );
            return false;
        }
    }


    private String getExternalIp()
    {
        try
        {
            if ( localPeer != null )
            {
                return localPeer.getExternalIp();
            }
            else
            {
                List<InetAddress> ips = InetUtils.getLocalIPAddresses();
                return ips.get( 0 ).getHostAddress();
            }
        }
        catch ( PeerException | SocketException | IndexOutOfBoundsException ex )
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
        bootstrap.addModule( new TrustedWebClientFactoryModule() );

        bootstrap.addModule( new QuotaManagementModule() );

        bootstrap.boot();

        return bootstrap.getInjector();
    }


    private Map<String, Object> convertToSimple( DefaultTemplate template, boolean deletable )
    {
        Map<String, Object> simple = new HashMap<>();
        simple.put( "name", template.getName() );
        simple.put( "id", template.getId() );
        simple.put( "parent", template.getParent() );
        simple.put( "architecture", template.getArchitecture() );
        simple.put( "version", template.getVersion() );
        simple.put( "deletable", deletable );
        simple.put( "owner_fprint", template.getOwnerFprint() );

        return simple;
    }


    private Map<String, Object> convertToSimple( SharedTemplate template, boolean deletable )
    {
        Map<String, Object> simple = new HashMap<>();
        simple.put( "name", template.getName() );
        simple.put( "id", template.getId() );
        simple.put( "parent", template.getParent() );
        simple.put( "architecture", template.getArchitecture() );
        simple.put( "version", template.getVersion() );
        simple.put( "deletable", deletable );
        simple.put( "owner_fprint", template.getOwnerFprint() );

        return simple;
    }


    /**
     * Gets cached metadata from the repositories of the supplied unified repository.
     */
    private Set<SerializableMetadata> listPackagesFromCache( UnifiedRepository repository )
    {
        Set<SerializableMetadata> result = new HashSet<>();

        Set<Repository> repos = new HashSet<>();
        repos.addAll( repository.getRepositories() );
        repos.addAll( repository.getSecondaryRepositories() );

        for ( Repository repo : repos )
        {
            if ( repo instanceof RemoteRepository )
            {
                RemoteRepository remote = ( RemoteRepository ) repo;
                List<SerializableMetadata> ls = remote.getMetadataCache().getMetadataList();
                result.addAll( ls );
            }
            else
            {
                List<SerializableMetadata> ls = repo.listPackages();
                result.addAll( ls );
            }
        }
        return result;
    }


    private TemplateKurjun convertToSubutaiTemplate( SubutaiTemplateMetadata meta )
    {
        TemplateKurjun template =
                new TemplateKurjun( String.valueOf( meta.getId() ), Hex.encodeHexString( meta.getMd5Sum() ),
                        meta.getName(), meta.getVersion(), meta.getArchitecture().name(), meta.getParent(),
                        meta.getPackage(), meta.getOwnerFprint() );

        template.setConfigContents( meta.getConfigContents() );
        template.setPackagesContents( meta.getPackagesContents() );
        template.setSize( meta.getSize() );

        return template;
    }
}

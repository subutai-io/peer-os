package io.subutai.core.kurjun.impl;


import io.subutai.core.kurjun.impl.store.RepoUrlStore;
import io.subutai.core.kurjun.impl.model.RepoUrl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

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
import ai.subut.kurjun.metadata.common.subutai.TemplateId;
import ai.subut.kurjun.metadata.common.utils.IdValidators;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreFactory;
import ai.subut.kurjun.metadata.factory.PackageMetadataStoreModule;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.metadata.template.SubutaiTemplateMetadata;
import ai.subut.kurjun.model.repository.LocalRepository;
import ai.subut.kurjun.model.repository.NonLocalRepository;
import ai.subut.kurjun.model.repository.Repository;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.quota.DataUnit;
import ai.subut.kurjun.quota.QuotaException;
import ai.subut.kurjun.quota.QuotaInfoStore;
import ai.subut.kurjun.quota.QuotaManagementModule;
import ai.subut.kurjun.quota.QuotaManagerFactory;
import ai.subut.kurjun.quota.disk.DiskQuota;
import ai.subut.kurjun.quota.disk.DiskQuotaManager;
import ai.subut.kurjun.quota.transfer.TransferQuota;
import ai.subut.kurjun.quota.transfer.TransferQuotaManager;
import ai.subut.kurjun.repo.LocalTemplateRepository;
import ai.subut.kurjun.repo.RepositoryFactory;
import ai.subut.kurjun.repo.RepositoryModule;
import ai.subut.kurjun.riparser.ReleaseIndexParserModule;
import ai.subut.kurjun.security.SecurityModule;
import ai.subut.kurjun.snap.SnapMetadataParserModule;
import ai.subut.kurjun.storage.factory.FileStoreFactory;
import ai.subut.kurjun.storage.factory.FileStoreModule;
import ai.subut.kurjun.subutai.SubutaiTemplateParserModule;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.SharedTemplate;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.kurjun.api.template.TemplateRepository;
import io.subutai.core.kurjun.impl.model.SharedTemplateInfo;
import io.subutai.core.kurjun.impl.model.UserRepoContext;
import io.subutai.core.kurjun.impl.store.UserRepoContextStore;
import io.subutai.core.object.relation.api.RelationManager;


@PermitAll
public class TemplateManagerImpl implements TemplateManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );

    private Set<UserRepoContext> GLOBAL_CONTEXTS;
    private Set<UserRepoContext> PRIVATE_CONTEXTS;

    // url list read from kurjun.cfg file on bundle start up
    private final List<String> globalKurjunUrlList = new ArrayList<>();

    private Injector injector;

    private Set<RepoUrl> remoteRepoUrls = new HashSet<>();

    // private Set<RepoUrl> globalRepoUrls = new LinkedHashSet<>();

    private final LocalPeer localPeer;

    private final RepoUrlStore repoUrlStore = new RepoUrlStore( Common.SUBUTAI_APP_DATA_PATH );

    private final UserRepoContextStore userRepoContextStore = new UserRepoContextStore( Common.SUBUTAI_APP_DATA_PATH );

    private ScheduledExecutorService metadataCacheUpdater;

    private final SubutaiSecurityHelper securityHelper;


    public TemplateManagerImpl( LocalPeer localPeer, IdentityManager identityManager, RelationManager relationManager,
            io.subutai.core.security.api.SecurityManager securityManager, String globalKurjunUrl )
    {
        this.localPeer = localPeer;
        this.securityHelper = new SubutaiSecurityHelper( identityManager, relationManager, securityManager );
        // parseGlobalKurjunUrls( globalKurjunUrl );
    }


    public void init()
    {
        injector = bootstrapDI();

        KurjunProperties properties = injector.getInstance( KurjunProperties.class );

        initRepoUrls();

        initUserRepoContexts( properties );

        logAllUrlsInUse();

        // schedule metadata cache updater
        metadataCacheUpdater = Executors.newSingleThreadScheduledExecutor();
        metadataCacheUpdater.scheduleWithFixedDelay( ()
                -> 
                {
                    for ( KurjunContext context : GLOBAL_CONTEXTS )
                    {
                        refreshMetadataCache( context.getName() );
                    }
        }, 5, 30, TimeUnit.SECONDS );
    }


    public void dispose()
    {
        metadataCacheUpdater.shutdown();
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
    public TemplateKurjun getTemplate( String repository, byte[] md5, String templateOwner, boolean isKurjunClient )
            throws IOException
    {
        UserRepoContext context = getUserRepoContext( repository );
        securityHelper.checkGetPermission( context, md5, templateOwner );

        DefaultTemplate m = new DefaultTemplate();
        m.setId( templateOwner, md5 );

        UnifiedRepository repo = getRepository( context, isKurjunClient );
        SubutaiTemplateMetadata meta = ( SubutaiTemplateMetadata ) repo.getPackageInfo( m );
        if ( meta != null )
        {
            return convertToSubutaiTemplate( meta );
        }
        return null;
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
    public TemplateKurjun getTemplate( String repository, String name, String version, boolean isKurjunClient )
            throws IOException
    {
        DefaultMetadata m = new DefaultMetadata();
        m.setName( name );
        m.setVersion( version );

        UserRepoContext context = getUserRepoContext( repository );
        UnifiedRepository repo = getRepository( context, isKurjunClient );

        SubutaiTemplateMetadata meta = ( SubutaiTemplateMetadata ) repo.getPackageInfo( m );
        if ( meta != null )
        {
            securityHelper.checkGetPermission( context, meta.getMd5Sum(), meta.getOwnerFprint() );
            return convertToSubutaiTemplate( meta );
        }
        return null;
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
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
    @RolesAllowed( "Template-Management|Read" )
    public InputStream getTemplateData( String repository, byte[] md5, String templateOwner, boolean isKurjunClient ) throws IOException
    {
        UserRepoContext context = getUserRepoContext( repository );
        securityHelper.checkGetPermission( context, md5, templateOwner );

        DefaultTemplate m = new DefaultTemplate();
        m.setId( templateOwner, md5 );

        UnifiedRepository repo = getRepository( context, isKurjunClient );
        InputStream is = repo.getPackageStream( m );

        if ( is != null )
        {
            QuotaManagerFactory quotaManagerFactory = injector.getInstance( QuotaManagerFactory.class );
            TransferQuotaManager qm = quotaManagerFactory.createTransferQuotaManager( context );
            return qm.createManagedStream( is );
        }
        return null;
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
    public List<TemplateKurjun> list( String repository, boolean isKurjunClient ) throws IOException
    {
        UserRepoContext context = getUserRepoContext( repository );
        UnifiedRepository repo = getRepository( context, isKurjunClient );
        Set<SerializableMetadata> metadatas = listPackagesFromCache( repo );

        List<TemplateKurjun> result = new LinkedList<>();

        for ( SerializableMetadata metadata : metadatas )
        {
            DefaultTemplate templateMeta = ( DefaultTemplate ) metadata;
            if ( securityHelper.isGetAllowed( context, templateMeta.getMd5Sum(), templateMeta.getOwnerFprint() ) )
            {
                result.add( convertToSubutaiTemplate( templateMeta ) );
            }
        }

        return result;
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
    public List<Map<String, Object>> getSharedTemplateInfos( byte[] md5, String templateOwner ) throws IOException
    {
        String md5Str = Hex.encodeHexString( md5 );
        List<SharedTemplateInfo> list = securityHelper.getSharedTemplateInfos( new TemplateId( templateOwner, md5Str ) );
        List<Map<String, Object>> shared = new ArrayList<>();

        for ( SharedTemplateInfo info : list )
        {
            Map<String, Object> simple = new HashMap<>();
            simple.put( "id", info.getTemplateId() );
            simple.put( "from_user", info.getFromUserFprint() );
            simple.put( "from_name", securityHelper.getUserName( info.getFromUserFprint() ) );
            simple.put( "to_user", info.getToUserfprint() );
            simple.put( "to_name", securityHelper.getUserName( info.getToUserfprint() ) );

            shared.add( simple );
        }

        return shared;
    }


    private List<DefaultTemplate> getSharedTemplates() throws IOException
    {
        String userFprint = securityHelper.getActiveUserFingerprint();
        List<SharedTemplateInfo> list = securityHelper.getSharedTemplatesToUser( userFprint );

        List<DefaultTemplate> shared = new ArrayList<>();
        for ( SharedTemplateInfo info : list )
        {
            UserRepoContext context = getUserRepoContext( info.getFromUserFprint() );
            LocalRepository repo = getLocalRepository( context );
            TemplateId id = IdValidators.Template.validate( info.getTemplateId() );
            DefaultTemplate m = new DefaultTemplate();
            m.setId( id.getOwnerFprint(), securityHelper.decodeMd5( id.getMd5() ) );
            DefaultTemplate meta = ( DefaultTemplate ) repo.getPackageInfo( m );
            if ( meta != null )
            {
                shared.add( meta );
            }
        }

        return shared;
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
    public List<Map<String, Object>> listAsSimple( String repository ) throws IOException
    {
        UserRepoContext context = getUserRepoContext( repository );
        List<Map<String, Object>> simpleList = new ArrayList<>();

        if ( TemplateRepository.SHARED.equals( context.getName() ) )
        {
            List<DefaultTemplate> sharedList = getSharedTemplates();
            for ( DefaultTemplate shared : sharedList )
            {
                simpleList.add( convertToSimple( shared, false ) );
            }
        }
        else
        {
            LocalRepository localRepo = getLocalRepository( context );
            List<SerializableMetadata> localList = localRepo.listPackages();
            String currFprint = securityHelper.getActiveUserFingerprint();

            for ( SerializableMetadata metadata : localList )
            {
                DefaultTemplate templateMeta = ( DefaultTemplate ) metadata;
                if ( securityHelper.isGetAllowed( context, templateMeta.getMd5Sum(), templateMeta.getOwnerFprint() ) )
                {
                    simpleList.add( convertToSimple( templateMeta, currFprint.equals( templateMeta.getOwnerFprint() ) ) );
                }
            }

            if ( !TemplateRepository.MY.equals( repository ) )
            {
                UnifiedRepository unifiedRepo = getRepository( context, false );
                List<SerializableMetadata> unifiedList = unifiedRepo.listPackages();

                unifiedList.removeAll( localList );

                for ( SerializableMetadata metadata : unifiedList )
                {
                    DefaultTemplate templateMeta = ( DefaultTemplate ) metadata;
                    if ( securityHelper.isGetAllowed( context, templateMeta.getMd5Sum(), templateMeta.getOwnerFprint() ) )
                    {
                        simpleList.add( convertToSimple( templateMeta, false ) );
                    }
                }
            }
        }

        return simpleList;
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
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
    @RolesAllowed( "Template-Management|Write" )
    public boolean isUploadAllowed( String repository )
    {
        UserRepoContext context = getUserRepoContext( repository );
        return securityHelper.isAddAllowed( context );
    }


    @Override
    @RolesAllowed( "Template-Management|Write" )
    public String upload( String repository, InputStream inputStream ) throws IOException
    {
        UserRepoContext context = getUserRepoContext( repository );
        securityHelper.checkAddPermission( context );

        LocalTemplateRepository repo = getLocalRepository( context );
        QuotaManagerFactory quotaManagerFactory = injector.getInstance( QuotaManagerFactory.class );
        DiskQuotaManager diskQuotaManager = quotaManagerFactory.createDiskQuotaManager( context );

        Path dump = null;
        try
        {
            dump = diskQuotaManager.copyStream( inputStream );
        }
        catch ( QuotaException ex )
        {
            throw new IOException( ex );
        }

        try ( InputStream is = new FileInputStream( dump.toFile() ) )
        {
            String ownerFprint = securityHelper.getActiveUserFingerprint();
            SubutaiTemplateMetadata m = ( SubutaiTemplateMetadata ) repo.put( is, CompressionType.GZIP, ownerFprint );

            TemplateId tid = new TemplateId( m.getOwnerFprint(), Hex.encodeHexString( m.getMd5Sum() ) );
            securityHelper.grantOwnerPermissions( tid, ownerFprint );

            return tid.get();
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to put template", ex );
        }
        finally
        {
            dump.toFile().delete();
        }
        return null;
    }


    @Override
    @RolesAllowed( "Template-Management|Delete" )
    public boolean delete( String repository, String templateOwner, byte[] md5 ) throws IOException
    {
        UserRepoContext context = getUserRepoContext( repository );
        securityHelper.checkDeletePermission( context, md5, templateOwner );

        LocalRepository repo = getLocalRepository( context );
        try
        {
            TemplateId tid = new TemplateId( templateOwner, Hex.encodeHexString( md5 ) );
            repo.delete( tid.get(), md5 );
            return true;
        }
        catch ( IOException ex )
        {
            LOGGER.error( "Failed to delete template", ex );
            return false;
        }
    }


    @Override
    public void shareTemplate( String templateIdStr, String targetUserName )
    {
        TemplateId templateId = IdValidators.Template.validate( templateIdStr );
        String targetUserFingerprint = securityHelper.getUserFingerprintByUsername( targetUserName );
        securityHelper.grantReadPermissionFromActiveUser( templateId, targetUserFingerprint );
    }


    @Override
    public void unshareTemplate( String templateIdStr, String targetUserName )
    {
        TemplateId templateId = IdValidators.Template.validate( templateIdStr );
        String targetUserFingerprint = securityHelper.getUserFingerprintByUsername( targetUserName );
        securityHelper.revokeReadPermission( templateId, targetUserFingerprint );
    }


    @Override
    public List<Map<String, Object>> getRemoteRepoUrls()
    {
        List<Map<String, Object>> urls = new ArrayList<>();
        try
        {
            for ( RepoUrl r : repoUrlStore.getRemoteTemplateUrls() )
            {
                Map<String, Object> map = new HashMap<>( 3 );
                map.put( "url", r.getUrl().toExternalForm() );
                map.put( "useToken", r.getToken() != null ? "yes" : "no" );
                map.put( "global", "no" );
                urls.add( map );
            }

            for ( RepoUrl r : getGlobalKurjunUrls() )
            {
                Map<String, Object> map = new HashMap<>( 3 );
                map.put( "url", r.getUrl().toExternalForm() );
                map.put( "useToken", r.getToken() != null ? "yes" : "no" );
                map.put( "global", "yes" );
                urls.add( map );
            }
        }
        catch ( IOException e )
        {
            LOGGER.error( "", e );
        }
        return urls;
    }


    @Override
    @RolesAllowed( "Template-Management|Write" )
    public void addRemoteRepository( URL url, String token )
    {
        try
        {
            if ( url != null && !url.getHost().equals( getExternalIp() ) )
            {
                repoUrlStore.addRemoteTemplateUrl( new RepoUrl( url, token ) );
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
    @RolesAllowed( "Template-Management|Delete" )
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
                remoteRepoUrls = repoUrlStore.getRemoteTemplateUrls();
            }
            catch ( IOException e )
            {
                LOGGER.error( "Failed to remove remote host url: {}", url, e );
            }
        }
    }


    @Override
    @RolesAllowed( "Template-Management|Read" )
    public Set<String> getRepositories()
    {
        Set<String> set = GLOBAL_CONTEXTS.stream().map( c -> c.getName() ).collect( Collectors.toSet() );
        if ( securityHelper.isUserHasKeyId( securityHelper.getActiveUserFingerprint() ) )
        {
            set.add( TemplateRepository.SHARED );
            set.add( TemplateRepository.MY );
        }
        return Collections.unmodifiableSet( set );
    }
  

    @Override
    @RolesAllowed( "Template-Management|Write" )
    public void createUserRepository( String userName )
    {
        String fprint = securityHelper.getUserFingerprintByUsername( userName );
        TemplateId privateRepositoryId = new TemplateId( fprint, fprint );
        securityHelper.grantOwnerPermissions( privateRepositoryId, fprint );
        UserRepoContext newcontext = new UserRepoContext( fprint, fprint );

        KurjunProperties properties = injector.getInstance( KurjunProperties.class );
        Properties kcp = properties.getContextProperties( newcontext );
        kcp.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
        kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE,
                PackageMetadataStoreFactory.FILE_DB );

        PRIVATE_CONTEXTS.add( newcontext );
        
        LOGGER.info( "Kurjun repository '{}' created for the userName {}", fprint, userName );
    }


    @Override
    public Long getDiskQuota( String repository )
    {
        KurjunContext c = getUserRepoContext( repository );
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
        KurjunContext kurjunContext = getUserRepoContext( repository );

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
        KurjunContext c = getUserRepoContext( repository );
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
        KurjunContext kurjunContext = getUserRepoContext( repository );

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
        bootstrap.addModule( new SecurityModule() );
        bootstrap.addModule( new QuotaManagementModule() );

        bootstrap.boot();

        return bootstrap.getInjector();
    }


    private void initRepoUrls()
    {
        try
        {
            // Load remote repo urls from store
            remoteRepoUrls = repoUrlStore.getRemoteTemplateUrls();

//            // Refresh global urls
//            repoUrlStore.removeAllGlobalTemplateUrl();
//            for ( String url : SystemSettings.getGlobalKurjunUrls() )
//            {
//                repoUrlStore.addGlobalTemplateUrl( new RepoUrl( new URL( url ), null ) );
//            }
//
//            // Load global repo urls from store
//            globalRepoUrls = repoUrlStore.getGlobalTemplateUrls();
        }
        catch ( IOException e )
        {
            LOGGER.error( "Failed to get remote repository URLs", e );
        }
    }
    
    
    private void initUserRepoContexts( KurjunProperties properties )
    {
        // init repo urls
        try
        {
            // Load user repository contexts from store
            GLOBAL_CONTEXTS = userRepoContextStore.getUserRepoContexts();

            // add default repository contexts
            // TODO: should we need to save default UserRepoContext ??
            GLOBAL_CONTEXTS.add( new UserRepoContext( TemplateRepository.PUBLIC, TemplateRepository.PUBLIC ) );
            GLOBAL_CONTEXTS.add( new UserRepoContext( TemplateRepository.TRUST, TemplateRepository.TRUST ) );

            // add user private repositories
            PRIVATE_CONTEXTS = new HashSet<>();
            PRIVATE_CONTEXTS.add( new UserRepoContext( TemplateRepository.SHARED, TemplateRepository.SHARED ) );
            
            List<String> fprints = securityHelper.getUserFingerprints();
            for ( String fprint : fprints )
            {
                // Treat private repos as a template object with id = owner fprint
                TemplateId privateRepositoryId = new TemplateId( fprint, fprint );
                securityHelper.grantOwnerPermissions( privateRepositoryId, fprint );
                PRIVATE_CONTEXTS.add( new UserRepoContext( fprint, fprint ) );
            }

            // init common
            for ( UserRepoContext kc : GLOBAL_CONTEXTS )
            {
                Properties kcp = properties.getContextProperties( kc );
                kcp.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
                kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE,
                        PackageMetadataStoreFactory.FILE_DB );
            }

            // init private
            for ( UserRepoContext kc : PRIVATE_CONTEXTS )
            {
                Properties kcp = properties.getContextProperties( kc );
                kcp.setProperty( FileStoreFactory.TYPE, FileStoreFactory.FILE_SYSTEM );
                kcp.setProperty( PackageMetadataStoreModule.PACKAGE_METADATA_STORE_TYPE,
                        PackageMetadataStoreFactory.FILE_DB );
            }
        }
        catch ( IOException e )
        {
            LOGGER.error( "Failed to get user repository contexts", e );
        }
    }


    private LocalTemplateRepository getLocalRepository( KurjunContext context ) throws IOException
    {
        try
        {
            RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
            return ( LocalTemplateRepository ) repositoryFactory.createLocalTemplate( context );
        }
        catch ( IllegalArgumentException ex )
        {
            throw new IOException( ex );
        }
    }


    private UnifiedRepository getRepository( KurjunContext context, boolean isKurjunClient ) throws IOException
    {
        RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
        UnifiedRepository unifiedRepo = repositoryFactory.createUnifiedRepo();
        unifiedRepo.getRepositories().add( getLocalRepository( context ) );

        if ( !isKurjunClient )
        {
            for ( RepoUrl repoUrl : remoteRepoUrls )
            {
                unifiedRepo.getRepositories().add( repositoryFactory
                        .createNonLocalTemplate( repoUrl.getUrl().toString(), null, context.getName(), repoUrl.getToken() ) );
            }

            // shuffle the global repo list to randomize and normalize usage of them
            List<RepoUrl> list = new ArrayList<>( getGlobalKurjunUrls() );
            Collections.shuffle( list );

            for ( RepoUrl repoUrl : list )
            {
                unifiedRepo.getSecondaryRepositories().add( repositoryFactory.createNonLocalTemplate(
                        repoUrl.getUrl().toString(), null, context.getName(), repoUrl.getToken() ) );
            }
        }
        return unifiedRepo;
    }

    
    private List<RepoUrl> getGlobalKurjunUrls()
    {
        try
        {
            List<RepoUrl> list = new ArrayList<>();
            for ( String url : SystemSettings.getGlobalKurjunUrls() )
            {
                String templateUrl = url + "/templates";
                list.add( new RepoUrl( new URL( templateUrl ), null ) );
            }
            return list;
        }
        catch ( MalformedURLException e )
        {
            throw new IllegalArgumentException( "Invalid global kurjun url", e );
        }
    }
    

    /**
     * Gets user repository context for templates repository.
     *
     * @return user repository context instance
     * @throws IllegalArgumentException if invalid/unknown repository value is supplied
     */
    private UserRepoContext getUserRepoContext( String repository )
    {
        String repo = TemplateRepository.MY.equals( repository )
                ? securityHelper.getActiveUserFingerprint() : repository;
        
        Set<UserRepoContext> set = GLOBAL_CONTEXTS;
        for ( UserRepoContext c : set )
        {
            if ( c.getName().equals( repo ) )
            {
                return c;
            }
        }

        set = PRIVATE_CONTEXTS;
        for ( UserRepoContext c : set )
        {
            if ( c.getName().equals( repo ) )
            {
                return c;
            }
        }
        throw new IllegalArgumentException( "Invalid repository " + repo );
    }


//    private void parseGlobalKurjunUrls( String globalKurjunUrl )
//    {
//        if ( !Strings.isNullOrEmpty( globalKurjunUrl ) )
//        {
//            String urls[] = globalKurjunUrl.split( "," );
//
//            for ( int x = 0; x < urls.length; x++ )
//            {
//                urls[x] = urls[x].trim();
//                globalKurjunUrlList.add( urls[x] );
//            }
//        }
//    }


    private void logAllUrlsInUse()
    {
        LOGGER.info( "Remote template urls:" );
        for ( RepoUrl r : remoteRepoUrls )
        {
            LOGGER.info( r.toString() );
        }

        for ( RepoUrl r : getGlobalKurjunUrls() )
        {
            LOGGER.info( r.toString() );
        }
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
        simple.put( "owner_name", securityHelper.getUserName( template.getOwnerFprint() ) );

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
        simple.put( "owner_name", securityHelper.getUserName( template.getOwnerFprint() ) );

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
            if ( repo instanceof NonLocalRepository )
            {
                NonLocalRepository remote = ( NonLocalRepository ) repo;
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


    /**
     * Refreshes metadata cache for each remote repository.
     */
    private void refreshMetadataCache( String repository )
    {
        Set<NonLocalRepository> remotes = new HashSet<>();
        RepositoryFactory repoFactory = injector.getInstance( RepositoryFactory.class );

        for ( RepoUrl url : remoteRepoUrls )
        {
            remotes.add( repoFactory.createNonLocalTemplate( url.getUrl().toString(), null, repository, url.getToken() ) );
        }
        for ( RepoUrl url : getGlobalKurjunUrls() )
        {
            remotes.add( repoFactory.createNonLocalTemplate( url.getUrl().toString(), null, repository, url.getToken() ) );
        }

        for ( NonLocalRepository remote : remotes )
        {
            remote.getMetadataCache().refresh();
        }
    }


    private TemplateKurjun convertToSubutaiTemplate( SubutaiTemplateMetadata meta )
    {
        TemplateKurjun template = new TemplateKurjun( String.valueOf( meta.getId() ),
                Hex.encodeHexString( meta.getMd5Sum() ), meta.getName(),
                meta.getVersion(), meta.getArchitecture().name(),
                meta.getParent(), meta.getPackage(), meta.getOwnerFprint() );
        template.setConfigContents( meta.getConfigContents() );
        template.setPackagesContents( meta.getPackagesContents() );
        return template;
    }

}

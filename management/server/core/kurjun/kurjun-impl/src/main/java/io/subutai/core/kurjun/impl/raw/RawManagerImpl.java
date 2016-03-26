package io.subutai.core.kurjun.impl.raw;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.subutai.core.kurjun.api.Utils;
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
    private LocalRawRepository localPublicRawRepository;
    private UnifiedRepository unifiedRepository;

    private Set<RepoUrl> remoteRepoUrls = new HashSet<>();

    private final RepoUrlStore repoUrlStore = new RepoUrlStore( Common.SUBUTAI_APP_DATA_PATH );

    private Injector injector;


    public RawManagerImpl()
    {
        injector = bootstrapDI();

        _local();
        _unified();
    }


    public void init()
    {
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


    private void _local()
    {
        this.repositoryFactory = injector.getInstance( RepositoryFactory.class );

        this.localPublicRawRepository =
                this.repositoryFactory.createLocalRaw( new KurjunContext( DEFAULT_RAW_REPO_NAME ) );
    }


    private void _unified()
    {
        this.unifiedRepository = this.repositoryFactory.createUnifiedRepo();
        unifiedRepository.getRepositories().add( this.localPublicRawRepository );
        //        unifiedRepository.getRepositories().addAll( artifactContext.getRemoteRawRepositories() );
    }


    @Override
    public String md5()
    {
        return Utils.MD5.toString( localPublicRawRepository.md5() );
    }


    @Override
    public RawMetadata getInfo( final String repository, final byte[] md5 )
    {

        return null;
    }


    @Override
    public boolean delete( String repository, final byte[] md5 )
    {
        DefaultMetadata defaultMetadata = new DefaultMetadata();
        defaultMetadata.setFingerprint( repository );
        defaultMetadata.setMd5sum( md5 );
        try
        {
            //***** Check permissions (DELETE) *****************
            //            if ( checkRepoPermissions( "raw", defaultMetadata.getId().toString(), Permission.Delete ) )
            //            {
            return localPublicRawRepository.delete( defaultMetadata.getId(), md5 );
            //            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public RawMetadata getInfo( final byte[] md5 )
    {
        DefaultMetadata metadata = new DefaultMetadata();
        metadata.setMd5sum( md5 );
        return ( RawMetadata ) unifiedRepository.getPackageInfo( metadata );
    }


    @Override
    public RawMetadata getInfo( final RawMetadata metadata )
    {

        return ( RawMetadata ) unifiedRepository.getPackageInfo( metadata );
    }


    @Override
    public RawMetadata put( final File file )
    {
        Metadata metadata = null;
        try
        {
            //            // *******CheckRepoOwner ***************
            //            relationManagerService.checkRelationOwner( userSession, "raw", RelationObjectType
            // .RepositoryRaw.getId() );
            //            //**************************************

            //***** Check permissions (WRITE) *****************
            //            if ( checkRepoPermissions( "raw", null, Permission.Write ) )
            //            {
            metadata = localPublicRawRepository.put( file, CompressionType.NONE, DEFAULT_RAW_REPO_NAME );

            //***** Build Relation ****************
            //                relationManagerService
            //                        .buildTrustRelation( userSession.getUser(), userSession.getUser(), metadata
            // .getId().toString(),
            //                                RelationObjectType.RepositoryContent.getId(),
            //                                relationManagerService.buildPermissions( 4 ) );
            //*************************************

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
            //            // *******CheckRepoOwner ***************
            //            relationManagerService.checkRelationOwner( userSession, "raw", RelationObjectType
            // .RepositoryRaw.getId() );
            //            //**************************************

            //***** Check permissions (WRITE) *****************
            //            if ( checkRepoPermissions( "raw", null, Permission.Write ) )
            //            {
            metadata = localPublicRawRepository.put( new FileInputStream( file ), CompressionType.NONE, repository );

            //***** Build Relation ****************
            //                relationManagerService
            //                        .buildTrustRelation( userSession.getUser(), userSession.getUser(), metadata
            // .getId().toString(),
            //                                RelationObjectType.RepositoryContent.getId(),
            //                                relationManagerService.buildPermissions( 4 ) );
            //                //*************************************
            //            }
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

        //        if ( userSession.getUser().equals( identityManagerService.getPublicUser() ) )
        //        {
        //            return null;
        //        }

        Metadata metadata = null;
        try
        {
            //            // *******CheckRepoOwner ***************
            //            relationManagerService.checkRelationOwner( userSession, "raw", RelationObjectType
            // .RepositoryRaw.getId() );
            //            //**************************************
            //
            //            //***** Check permissions (WRITE) *****************
            //            if ( checkRepoPermissions( "raw", null, Permission.Write ) )
            //            {
            LocalRawRepository localRawRepository = getLocalPublicRawRepository( new KurjunContext( repository ) );
            metadata = localRawRepository.put( file, filename, repository );
            //
            //                //***** Build Relation ****************
            //                relationManagerService
            //                        .buildTrustRelation( userSession.getUser(), userSession.getUser(), metadata
            // .getId().toString(),
            //                                RelationObjectType.RepositoryContent.getId(),
            //                                relationManagerService.buildPermissions( 4 ) );
            //                //*************************************
            //            }
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
    public InputStream getFile( final String repository, final byte[] md5 ) throws IOException
    {
        return null;
    }


    @Override
    public List<SerializableMetadata> list( String repository )
    {
        List<RawMetadata> rawMetadatas;

        switch ( repository )
        {
            //return local list
            case "public":
                return localPublicRawRepository.listPackages();
            //return unified repo list
            case "all":
                return unifiedRepository.listPackages();
            //return personal repository list
            default:
                return repositoryFactory.createLocalTemplate( new KurjunContext( repository ) ).listPackages();
        }
    }


    @Override
    public boolean delete( final byte[] md5 ) throws IOException
    {
        return false;
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
                        .createNonLocalTemplate( repoUrl.getUrl().toString(), null, context.getName(),
                                repoUrl.getToken() ) );
            }

            // shuffle the global repo list to randomize and normalize usage of them
            List<RepoUrl> list = new ArrayList<>( getGlobalKurjunUrls() );
            Collections.shuffle( list );

            for ( RepoUrl repoUrl : list )
            {
                unifiedRepo.getSecondaryRepositories().add( repositoryFactory
                        .createNonLocalTemplate( repoUrl.getUrl().toString(), null, context.getName(),
                                repoUrl.getToken() ) );
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
                String templateUrl = url + RAW_PATH;
                list.add( new RepoUrl( new URL( templateUrl ), null ) );
            }
            return list;
        }
        catch ( MalformedURLException e )
        {
            throw new IllegalArgumentException( "Invalid global kurjun url", e );
        }
    }

    private LocalRawRepository getLocalRepository( KurjunContext context ) throws IOException
    {
        try
        {
            RepositoryFactory repositoryFactory = injector.getInstance( RepositoryFactory.class );
            return repositoryFactory.createLocalRaw( context );
        }
        catch ( IllegalArgumentException ex )
        {
            throw new IOException( ex );
        }
    }
}

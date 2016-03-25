package io.subutai.core.kurjun.impl.raw;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.subut.kurjun.ar.CompressionType;
import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.metadata.common.DefaultMetadata;
import ai.subut.kurjun.metadata.common.raw.RawMetadata;
import ai.subut.kurjun.model.identity.UserSession;
import ai.subut.kurjun.model.metadata.Metadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import ai.subut.kurjun.model.repository.UnifiedRepository;
import ai.subut.kurjun.repo.LocalRawRepository;
import ai.subut.kurjun.repo.RepositoryFactory;
import io.subutai.core.kurjun.api.RepositoryContext;
import io.subutai.core.kurjun.api.Utils;
import io.subutai.core.kurjun.api.raw.RawManager;
import io.subutai.core.kurjun.impl.TemplateManagerImpl;


public class RawManagerImpl implements RawManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateManagerImpl.class );
    private static final String DEFAULT_RAW_REPO_NAME = "raw";

    private RepositoryFactory repositoryFactory;
    private LocalRawRepository localPublicRawRepository;
    private UnifiedRepository unifiedRepository;
    private RepositoryContext artifactContext;

    private UserSession userSession;


    public RawManagerImpl()
    {
        _local();
        _unified();
    }


    private void _local()
    {
        this.localPublicRawRepository =
                this.repositoryFactory.createLocalRaw( new KurjunContext( DEFAULT_RAW_REPO_NAME ) );
    }


    private void _unified()
    {
        this.unifiedRepository = this.repositoryFactory.createUnifiedRepo();
        unifiedRepository.getRepositories().add( this.localPublicRawRepository );
        unifiedRepository.getRepositories().addAll( artifactContext.getRemoteRawRepositories() );
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
}

package org.safehaus.subutai.core.registry.rest;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService
{

    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final String EXCEPTION_HEADER = "exception";
    private static final String TEMPLATE_PARENT_DELIMITER = " ";
    private static final String TEMPLATES_DELIMITER = "\n";

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private final TemplateRegistry templateRegistry;
    private final RepositoryManager repositoryManager;
    private final PeerManager peerManager;


    public RestServiceImpl( final RepositoryManager repositoryManager, final TemplateRegistry templateRegistry,
                            final PeerManager peerManager )
    {
        Preconditions.checkNotNull( repositoryManager );
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );

        this.repositoryManager = repositoryManager;
        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
    }


    @Override
    public Response downloadTemplate( final String templateName )
    {
        try
        {
            String packageName = String.format( "%s-subutai-template", templateName );
            String fullPackageName = repositoryManager.getFullPackageName( packageName );
            String fullPackagePath =
                    String.format( "%s%s%s", Common.APT_REPO_PATH, Common.APT_REPO_AMD64_PACKAGES_SUBPATH,
                            fullPackageName );

            File packageFile = new File( fullPackagePath );

            if ( packageFile.exists() )
            {
                if ( packageFile.isFile() )
                {
                    return Response.ok( packageFile ).header( "Content-Disposition",
                            String.format( "attachment; filename=%s", fullPackageName ) ).build();
                }
                else
                {
                    return Response.status( Response.Status.BAD_REQUEST ).entity( "File is directory" ).build();
                }
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( RepositoryException e )
        {
            LOG.error( "Error in downloadTemplate", e );
            return Response.serverError().entity( e ).build();
        }
    }


    @Override
    public Response getTemplate( final String templateName )
    {
        Template template = templateRegistry.getTemplate( templateName );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response registerTemplate( final String configFilePath, final String packagesFilePath, final String md5sum )
    {
        try
        {

            templateRegistry.registerTemplate( FileUtil.readFile( configFilePath, Charset.defaultCharset() ),
                    FileUtil.readFile( packagesFilePath, Charset.defaultCharset() ), md5sum );

            return Response.ok().build();
        }
        catch ( IOException | RegistryException | RuntimeException e )
        {
            LOG.error( "Error in registerTemplate", e );
            return Response.status( Response.Status.BAD_REQUEST ).header( EXCEPTION_HEADER, e.getMessage() ).build();
        }
    }


    private String getTempDirPath()
    {
        return System.getProperty( "java.io.tmpdir" );
    }


    @Override
    public Response importTemplate( final Attachment attachment, String configDir )
    {
        String packageName = attachment.getContentDisposition().getParameter( "filename" );

        Path path = Paths.get( getTempDirPath(), packageName );

        try
        {

            //save file to temp directory
            InputStream in = attachment.getObject( InputStream.class );
            try
            {
                Files.copy( in, path, StandardCopyOption.REPLACE_EXISTING );
            }
            catch ( IOException e )
            {
                String m = "Failed to write payload data to file";
                LOG.error( m, e );
                return Response.serverError().entity( e ).build();
            }

            //add package to repository
            try
            {
                repositoryManager.addPackageByPath( path.toString() );
            }
            catch ( RepositoryException e )
            {
                String m = "Failed to add package to repository";
                LOG.error( m, e );
                return Response.serverError().entity( e ).build();
            }

            //trim leading slash
            if ( configDir.charAt( 0 ) == '/' )
            {
                configDir = configDir.substring( 1 );
            }

            //extract config and packages files and read their content
            Path configPath = Paths.get( configDir, "config" );
            Path packagesPath = Paths.get( configDir, "packages" );

            Set<String> files = new HashSet<>();
            files.add( configPath.toString() );
            files.add( packagesPath.toString() );

            try
            {
                repositoryManager.extractPackageFiles( packageName, files );
            }
            catch ( RepositoryException e )
            {
                String m = "Failed to extract template metadata from package";
                LOG.error( m, e );
                return Response.serverError().entity( e ).build();
            }

            //register template
            try
            {
                ManagementHost managementHost = peerManager.getLocalPeer().getManagementHost();

                String configContent = managementHost.readFile(
                        String.format( "/tmp/%s/%s", packageName.replace( ".deb", "" ), configPath.toString() ) );

                String packagesContent = managementHost.readFile(
                        String.format( "/tmp/%s/%s", packageName.replace( ".deb", "" ), packagesPath.toString() ) );

                //calculate md5sum of template
                HashCode md5 = com.google.common.io.Files.hash( path.toFile(), Hashing.md5() );
                String md5sum = md5.toString();

                //register template with registry
                templateRegistry.registerTemplate( configContent, packagesContent, md5sum );
            }
            catch ( Exception e )
            {
                String m = "Failed to register template";
                LOG.error( m, e );
                return Response.serverError().entity( e ).build();
            }
        }
        finally
        {
            // clean up
            if ( path != null )
            {
                path.toFile().delete();
            }
        }

        //all ok
        LOG.info( "Template package successfully imported." );
        return Response.ok().build();
    }


    @Override
    public Response unregisterTemplate( final String templateName )
    {

        //check if template exists
        if ( templateRegistry.getTemplate( templateName ) == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }

        //unregister template from registry
        try
        {
            templateRegistry.unregisterTemplate( templateName );

            return Response.ok().build();
        }
        catch ( RegistryException e )
        {
            LOG.error( "Error in unregisterTemplate", e );
            return Response.serverError().entity( e ).build();
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Error in unregisterTemplate", e );
            return Response.status( Response.Status.BAD_REQUEST ).header( EXCEPTION_HEADER, e.getMessage() ).build();
        }
    }


    @Override
    public Response removeTemplate( final String templateName )
    {
        Response response = unregisterTemplate( templateName );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            try
            {
                String packageName = String.format( "%s-subutai-template", templateName );
                String fullPackageName = repositoryManager.getFullPackageName( packageName );
                repositoryManager.removePackageByName( fullPackageName );
                return Response.ok().build();
            }
            catch ( RepositoryException e )
            {
                LOG.error( "Error in removeTemplate", e );
                return Response.serverError().entity( e ).build();
            }
        }
        else
        {
            return response;
        }
    }


    @Override
    public Response getTemplate( final String templateName, final String lxcArch )
    {
        Template template = templateRegistry.getTemplate( templateName, lxcArch );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplate( final String childTemplateName )
    {
        Template template = templateRegistry.getParentTemplate( childTemplateName );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplate( final String childTemplateName, final String lxcArch )
    {
        Template template = templateRegistry.getParentTemplate( childTemplateName, lxcArch );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplates( final String childTemplateName )
    {
        List<String> parents = new ArrayList<>();
        for ( Template template : templateRegistry.getParentTemplates( childTemplateName ) )
        {
            parents.add( template.getTemplateName() );
        }

        if ( !parents.isEmpty() )
        {
            return Response.ok().entity( GSON.toJson( parents ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplates( final String childTemplateName, final String lxcArch )
    {
        List<String> parents = new ArrayList<>();
        for ( Template template : templateRegistry.getParentTemplates( childTemplateName, lxcArch ) )
        {
            parents.add( template.getTemplateName() );
        }
        if ( !parents.isEmpty() )
        {
            return Response.ok().entity( GSON.toJson( parents ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getChildTemplates( final String parentTemplateName )
    {
        List<String> children = new ArrayList<>();
        for ( Template template : templateRegistry.getChildTemplates( parentTemplateName ) )
        {
            children.add( template.getTemplateName() );
        }
        if ( !children.isEmpty() )
        {
            return Response.ok().entity( GSON.toJson( children ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getChildTemplates( final String parentTemplateName, final String lxcArch )
    {
        List<String> children = new ArrayList<>();
        for ( Template template : templateRegistry.getChildTemplates( parentTemplateName, lxcArch ) )
        {
            children.add( template.getTemplateName() );
        }
        if ( !children.isEmpty() )
        {
            return Response.ok().entity( GSON.toJson( children ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getTemplateTree()
    {
        TemplateTree tree = templateRegistry.getTemplateTree();
        List<Template> uberTemplates = tree.getRootTemplates();
        if ( uberTemplates != null )
        {
            for ( Template template : uberTemplates )
            {
                addChildren( tree, template );
            }
            return Response.ok().entity( GSON.toJson( uberTemplates ) ).build();
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response isTemplateInUse( final String templateName )
    {
        try
        {
            return Response.ok().entity( JsonUtil.toJson( "RESULT", templateRegistry.isTemplateInUse( templateName ) ) )
                           .build();
        }
        catch ( RegistryException e )
        {
            LOG.error( "Error in isTemplateInUse", e );
            return Response.status( Response.Status.NOT_FOUND ).header( EXCEPTION_HEADER, e.getMessage() ).build();
        }
    }


    @Override
    public Response setTemplateInUse( final String faiHostname, final String templateName, final String isInUse )
    {
        try
        {

            templateRegistry.updateTemplateUsage( faiHostname, templateName, Boolean.parseBoolean( isInUse ) );

            return Response.ok().build();
        }
        catch ( RegistryException e )
        {
            LOG.error( "Error in setTemplateInUse", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).header( EXCEPTION_HEADER, e.getMessage() )
                           .build();
        }
    }


    @Override
    public Response listTemplates()
    {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistry.getAllTemplates() )
        {
            templates.add( template.getTemplateName() );
        }
        if ( !templates.isEmpty() )
        {
            return Response.ok().entity( GSON.toJson( templates ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response listTemplates( final String lxcArch )
    {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistry.getAllTemplates( lxcArch ) )
        {
            templates.add( template.getTemplateName() );
        }
        if ( !templates.isEmpty() )
        {
            return Response.ok().entity( GSON.toJson( templates ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response listTemplatesPlain()
    {
        return listTemplatesPlain( Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public Response listTemplatesPlain( final String lxcArch )
    {
        StringBuilder output = new StringBuilder();
        List<Template> templates = templateRegistry.getAllTemplates( lxcArch );

        if ( !templates.isEmpty() )
        {
            for ( final Template template : templates )
            {
                output.append( template.getTemplateName() ).append( TEMPLATE_PARENT_DELIMITER ).append(
                        Strings.isNullOrEmpty( template.getParentTemplateName() ) ? "" :
                        template.getParentTemplateName() ).append( TEMPLATES_DELIMITER );
            }

            return Response.ok().entity( GSON.toJson( output.toString() ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    private void addChildren( TemplateTree tree, Template currentTemplate )
    {
        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( !( children == null || children.isEmpty() ) )
        {
            currentTemplate.addChildren( children );
            for ( Template child : children )
            {
                addChildren( tree, child );
            }
        }
    }
}

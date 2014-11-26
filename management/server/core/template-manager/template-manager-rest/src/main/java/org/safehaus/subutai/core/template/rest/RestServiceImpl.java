package org.safehaus.subutai.core.template.rest;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.safehaus.subutai.core.template.api.TemplateManager;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;


public class RestServiceImpl implements RestService
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    TemplateManager templateManager;
    TemplateRegistry templateRegistry;
    RepositoryManager repoManager;
    //    AgentManager agentManager;
    CommandRunner commandRunner;
    PeerManager peerManager;

    private String managementHostName = "management";


    public void setTemplateManager( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    public void setTemplateRegistry( TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public void setRepoManager( RepositoryManager repoManager )
    {
        this.repoManager = repoManager;
    }


    //    public void setAgentManager( AgentManager agentManager )
    //    {
    //        this.agentManager = agentManager;
    //    }


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public String getManagementHostName()
    {
        return managementHostName;
    }


    @Override
    public void setManagementHostName( String managementHostName )
    {
        this.managementHostName = managementHostName;
    }


    @Override
    public Response importTemplate( final Attachment attachment, String configDir )
    {
        if ( configDir.charAt( 0 ) == '/' )
        {
            configDir = configDir.substring( 1 );
        }
        String packageName = attachment.getContentDisposition().getParameter( "filename" );
        Path path;
        try
        {
            path = Files.createTempFile( "subutai-template-", ".deb" );
            InputStream in = attachment.getObject( InputStream.class );
            try ( OutputStream os = new FileOutputStream( path.toFile() ) )
            {
                int len;
                byte[] buf = new byte[1024];
                while ( ( len = in.read( buf ) ) > 0 )
                {
                    os.write( buf, 0, len );
                }
                os.flush();
            }
            LOG.info( "Payload saved to " + path.toString() );
        }
        catch ( IOException ex )
        {
            String m = "Failed to write payload data to file";
            LOG.error( m, ex );
            return Response.serverError().build();
        }

        //        Agent mgmt = agentManager.getAgentByHostname( managementHostName );
        try
        {
            Path configPath = Paths.get( configDir, "config" );
            Path packagesPath = Paths.get( configDir, "packages" );

            Set<String> files = new HashSet<>();
            files.add( configPath.toString() );
            files.add( packagesPath.toString() );

            repoManager.extractPackageFiles( packageName, files );

            ManagementHost managementHost = peerManager.getLocalPeer().getManagementHost();

            String configContent = managementHost.readFile(
                    String.format( "/tmp/%s/%s", packageName.replace( ".deb", "" ), configPath.toString() ) );

            String packagesContent = managementHost.readFile(
                    String.format( "/tmp/%s/%s", packageName.replace( ".deb", "" ), packagesPath.toString() ) );

            //            List<String> files = repoManager.readFileContents( mgmt, path.toString(),
            //                    Arrays.asList( configPath.toString(), packagesPath.toString() ) );

            HashCode md5 = com.google.common.io.Files.hash( path.toFile(), Hashing.md5() );
            String md5sum = md5.toString();

            String templateName = retrieveTemplateName( configContent );
            String debPack = templateManager.getDebianPackageName( templateName );
            path = movePath( path, debPack + ".deb" );
            if ( path == null )
            {
                throw new Exception( "Failed to rename uploaded package" );
            }

            repoManager.addPackageByPath( path.toString() );
            templateRegistry.registerTemplate( configContent, packagesContent, md5sum );
        }
        catch ( RepositoryException ex )
        {
            String m = "Failed to process deb package";
            LOG.error( m, ex );
            return Response.serverError().build();
        }
        catch ( Exception ex )
        {
            String m = "Import of package failed";
            LOG.error( m, ex );
            return Response.serverError().build();
        }
        finally
        {
            // clean up
            if ( path != null )
            {
                path.toFile().delete();
            }
        }
        LOG.info( "Template package successfully imported." );
        return Response.ok().build();
    }


    @Override
    public Response exportTemplate( String templateName )
    {
        // TODO: we need to be able to export in specified physical sever
        // currently this method calls export script on management server
        String path = templateManager.exportTemplate( managementHostName, templateName );
        if ( path == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

        String cd = "attachment; filename=\"" + Paths.get( path ).getFileName() + "\"";
        return Response.ok( new File( path ) ).
                header( "Content-Disposition", cd ).
                               type( MediaType.APPLICATION_OCTET_STREAM_TYPE ).build();
    }


    @Override
    public Response unregister( String templateName )
    {
        try
        {
            templateRegistry.unregisterTemplate( templateName );
            LOG.info( String.format( "Template unregistered: %s", templateName ) );
        }
        catch ( RegistryException ex )
        {
            LOG.error( "Failed to unregister template", ex );
            return Response.serverError().build();
        }

        String pack_name = templateManager.getPackageName( templateName );
        //        Agent mgmt = agentManager.getAgentByHostname( managementHostName );
        try
        {
            repoManager.removePackageByName( pack_name );
            LOG.info( String.format( "Package removed from repository: %s", pack_name ) );

            removeDebianPackage( templateName );
        }
        catch ( RepositoryException ex )
        {
            LOG.error( "Failed to remove from repo", ex );
            return Response.serverError().build();
        }

        return Response.ok().build();
    }


    private void removeDebianPackage( String templateName )
    {
        String deb_pack = templateManager.getDebianPackageName( templateName );
        if ( deb_pack == null )
        {
            LOG.error( "Can't get Debian package name for {0}", templateName );
            return;
        }
        deb_pack += ".deb";

        Path path = Paths.get( Common.APT_REPO_PATH, Common.APT_REPO_AMD64_PACKAGES_SUBPATH, deb_pack );
        try
        {
            Files.delete( path );
            LOG.info( "Removed package file {0}", path );
        }
        catch ( IOException ex )
        {
            LOG.error( "Failed to remove package file " + deb_pack, ex );
        }
    }


    private String retrieveTemplateName( String config )
    {
        Properties prop = new Properties();
        try
        {
            prop.load( new StringReader( config ) );
            return prop.getProperty( "lxc.utsname" );
        }
        catch ( IOException ex )
        {
            LOG.error( "Failed to retrieve template name from config: {}", ex.getMessage() );
        }
        return null;
    }


    private Path movePath( Path path, String newName )
    {
        Path new_path = Paths.get( path.getParent().toString(), newName );
        try
        {
            Files.move( path, new_path, StandardCopyOption.REPLACE_EXISTING );
            return new_path;
        }
        catch ( IOException ex )
        {
            LOG.error( "Failed to move package file: {0}", ex.getMessage() );
        }
        return null;
    }
}

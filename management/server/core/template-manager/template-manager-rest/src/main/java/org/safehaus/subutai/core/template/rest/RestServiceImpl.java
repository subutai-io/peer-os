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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.template.api.TemplateManager;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;


public class RestServiceImpl implements RestService
{

    private static final Logger logger = Logger.getLogger( RestServiceImpl.class.getName() );
    TemplateManager templateManager;
    TemplateRegistry templateRegistry;
    AptRepositoryManager aptRepoManager;
    AgentManager agentManager;
    CommandRunner commandRunner;
    private String managementHostName = "management";


    public void setTemplateManager( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    public void setTemplateRegistry( TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public void setAptRepoManager( AptRepositoryManager aptRepoManager )
    {
        this.aptRepoManager = aptRepoManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
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
    public Response importTemplate( InputStream in, String configDir )
    {
        Path path;
        try
        {
            path = Files.createTempFile( "subutai-template-", ".deb" );
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
            logger.info( "Payload saved to " + path.toString() );
        }
        catch ( IOException ex )
        {
            String m = "Failed to write payload data to file";
            logger.log( Level.SEVERE, m, ex );
            return Response.serverError().build();
        }

        Agent mgmt = agentManager.getAgentByHostname( managementHostName );
        try
        {
            Path configPath = Paths.get( configDir, "config" );
            Path packagesPath = Paths.get( configDir, "packages" );
            List<String> files = aptRepoManager.readFileContents( mgmt, path.toString(),
                    Arrays.asList( configPath.toString(), packagesPath.toString() ) );

            HashCode md5 = com.google.common.io.Files.hash( path.toFile(), Hashing.md5() );
            String md5sum = md5.toString();

            String templateName = retrieveTemplateName( files.get( 0 ) );
            String debPack = templateManager.getDebianPackageName( templateName );
            path = movePath( path, debPack + ".deb" );
            if ( path == null )
            {
                throw new Exception( "Failed to rename uploaded package" );
            }

            aptRepoManager.addPackageByPath( mgmt, path.toString(), false );
            templateRegistry.registerTemplate( files.get( 0 ), files.get( 1 ), md5sum );
        }
        catch ( AptRepoException ex )
        {
            String m = "Failed to process deb package";
            logger.log( Level.SEVERE, m, ex );
            return Response.serverError().build();
        }
        catch ( Exception ex )
        {
            String m = "Import of package failed";
            logger.log( Level.SEVERE, m, ex );
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
        logger.info( "Template package successfully imported." );
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
            logger.info( String.format( "Template unregistered: %s", templateName ) );
        }
        catch ( RegistryException ex )
        {
            logger.log( Level.SEVERE, "Failed to unregister template", ex );
            return Response.serverError().build();
        }

        String pack_name = templateManager.getPackageName( templateName );
        Agent mgmt = agentManager.getAgentByHostname( managementHostName );
        try
        {
            aptRepoManager.removePackageByName( mgmt, pack_name );
            logger.info( String.format( "Package removed from repository: %s", pack_name ) );

            removeDebianPackage( templateName );
        }
        catch ( AptRepoException ex )
        {
            logger.log( Level.SEVERE, "Failed to remove from repo", ex );
            return Response.serverError().build();
        }

        return Response.ok().build();
    }


    private void removeDebianPackage( String templateName )
    {
        String deb_pack = templateManager.getDebianPackageName( templateName );
        if ( deb_pack == null )
        {
            logger.log( Level.WARNING, "Can't get Debian package name for {0}", templateName );
            return;
        }
        deb_pack += ".deb";

        Path path = Paths.get( Common.APT_REPO_PATH, Common.APT_REPO_AMD64_PACKAGES_SUBPATH, deb_pack );
        try
        {
            Files.delete( path );
            logger.log( Level.INFO, "Removed package file {0}", path );
        }
        catch ( IOException ex )
        {
            logger.log( Level.SEVERE, "Failed to remove package file " + deb_pack, ex );
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
            logger.log( Level.SEVERE, "Failed to retrieve template name from config: {}", ex.getMessage() );
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
            logger.log( Level.SEVERE, "Failed to move package file: {0}", ex.getMessage() );
        }
        return null;
    }
}

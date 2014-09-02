package org.safehaus.subutai.rest.template.manager;


import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.api.templateregistry.RegistryException;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.common.protocol.Agent;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RestServiceImpl implements RestService
{

    private static final Logger logger = Logger.getLogger( RestServiceImpl.class.getName() );
    TemplateManager templateManager;
    TemplateRegistryManager templateRegistry;
    AptRepositoryManager aptRepoManager;
    AgentManager agentManager;
    private String managementHostName = "management";


    public void setTemplateManager( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    public void setTemplateRegistry( TemplateRegistryManager templateRegistry )
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
            try (OutputStream os = new FileOutputStream( path.toFile() ))
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
            aptRepoManager.addPackageByPath( mgmt, path.toString(), false );

            Path configPath = Paths.get( configDir, "config" );
            Path packagesPath = Paths.get( configDir, "packages" );
            List<String> files = aptRepoManager.readFileContents( mgmt, path.toString(),
                Arrays.asList( configPath.toString(), packagesPath.toString() ) );

            HashCode md5 = com.google.common.io.Files.hash( path.toFile(), Hashing.md5() );
            String md5sum = md5.toString();

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
            path.toFile().delete();
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
        }
        catch ( AptRepoException ex )
        {
            logger.log( Level.SEVERE, "Failed to remove from repo", ex );
            return Response.serverError().build();
        }

        return Response.ok().build();
    }
}

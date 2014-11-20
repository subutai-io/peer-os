package org.safehaus.subutai.core.script.rest;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.collect.Sets;


/**
 * Implementation of Script Manager Rest
 */
public class ScriptManagerRestImpl implements ScriptManagerRest
{
    private static final String SCRIPTS_DIRECTORY = "/var/lib/subutai/pre-post-scripts/";
    protected String scriptsDirectoryPath = SCRIPTS_DIRECTORY;


    @Override
    public Response uploadScript( final Attachment scriptFile )
    {
        String scriptFileName = scriptFile.getContentDisposition().getParameter( "filename" );

        File scriptsDirectory = new File( scriptsDirectoryPath );
        scriptsDirectory.mkdirs();

        Path path = Paths.get( scriptsDirectoryPath, scriptFileName );


        InputStream in = scriptFile.getObject( InputStream.class );

        try
        {
            Files.copy( in, path, StandardCopyOption.REPLACE_EXISTING );

            return Response.ok().build();
        }
        catch ( IOException e )
        {
            return Response.serverError().entity( e ).build();
        }
    }


    @Override
    public Response removeScript( final String scriptName )
    {
        File scriptFile = new File( scriptsDirectoryPath + scriptName );

        if ( scriptFile.exists() )
        {
            if ( scriptFile.isFile() )
            {
                if ( scriptFile.delete() )
                {

                    return Response.ok().build();
                }
                else
                {
                    return Response.serverError().entity( "Could not delete script" ).build();
                }
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


    @Override
    public Response downloadScript( final String scriptName )
    {
        File scriptFile = new File( scriptsDirectoryPath + scriptName );

        if ( scriptFile.exists() )
        {
            if ( scriptFile.isFile() )
            {
                return Response.ok( scriptFile )
                               .header( "Content-Disposition", String.format( "attachment; filename=%s", scriptName ) )
                               .build();
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


    @Override
    public Response listScripts()
    {
        Set<String> scriptNames = Sets.newHashSet();
        File scriptsDirectory = new File( scriptsDirectoryPath );

        if ( scriptsDirectory.exists() && scriptsDirectory.listFiles() != null )
        {
            for ( final File fileEntry : scriptsDirectory.listFiles() )
            {
                if ( !fileEntry.isDirectory() )
                {
                    scriptNames.add( fileEntry.getName() );
                }
            }
        }

        return Response.ok( JsonUtil.toJson( scriptNames ) ).build();
    }
}

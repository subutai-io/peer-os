package org.safehaus.subutai.core.script.rest;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;


/**
 * Implementation of Script Manager Rest
 */
public class ScriptManagerRestImpl implements ScriptManagerRest
{
    private static final String SCRIPTS_DIRECTORY = "/var/lib/subutai/pre-post-scripts/";


    @Override
    public Response uploadScript( final Attachment scriptFile )
    {
        String scriptFileName = scriptFile.getContentDisposition().getParameter( "filename" );

        File dir = new File( SCRIPTS_DIRECTORY );
        dir.mkdirs();

        Path path = Paths.get( SCRIPTS_DIRECTORY + scriptFileName );


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
    public Response downloadScript( final String scriptName )
    {
        File scriptFile = new File( SCRIPTS_DIRECTORY + scriptName );

        if ( scriptFile.exists() )
        {
            return Response.ok( scriptFile )
                           .header( "Content-Disposition", String.format( "attachment; filename=%s", scriptName ) )
                           .build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }
}

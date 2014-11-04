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
    @Override
    public Response uploadFile( final Attachment attachment )
    {
        String filename = attachment.getContentDisposition().getParameter( "filename" );

        File dir = new File( "/var/lib/subutai/pre-post-scripts/" );
        dir.mkdirs();

        Path path = Paths.get( "/var/lib/subutai/pre-post-scripts/" + filename );


        InputStream in = attachment.getObject( InputStream.class );

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
}

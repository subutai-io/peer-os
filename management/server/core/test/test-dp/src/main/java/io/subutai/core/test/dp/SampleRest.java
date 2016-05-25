package io.subutai.core.test.dp;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ape-craft on 5/24/16.
 */
public class SampleRest
{
    private static final Logger logger = LoggerFactory.getLogger( SampleRest.class );


    @GET
    @Path( "/books/pdf" )
    @Produces( "application/text" )
    public Response getPdf()
    {
        final String filename = "information.html";
        StreamingOutput sOut = new StreamingOutput()
        {
            public void write( final OutputStream os )
            {
                Integer counter = 0;
                WritableByteChannel outChannel = Channels.newChannel( os );

                URL website = null;
                try
                {
                    website = new URL( "http://bndtools.org/installation.html" );
                    URLConnection conn = website.openConnection();
                    conn.connect();

                    ReadableByteChannel rbc = Channels.newChannel( conn.getInputStream() );

                    ByteBuffer byteBuffer = ByteBuffer.allocate( 8192 );
                    FileOutputStream fos = new FileOutputStream( filename );
                    FileChannel fChannel = fos.getChannel();

                    int bytesRead = rbc.read( byteBuffer );

                    while ( bytesRead > 0 )
                    {

                        //limit is set to current position and position is set to zero
                        byteBuffer.flip();
                        ByteBuffer copy = byteBuffer.duplicate();
                        while ( copy.hasRemaining() )
                        {
                            os.write( copy.get() );
                            os.flush();
                            counter++;
                        }

                        while ( byteBuffer.hasRemaining() )
                        {
                            fChannel.write( byteBuffer );
                        }

                        byteBuffer.clear();
                        bytesRead = rbc.read( byteBuffer );
                        Thread.sleep( 2000 );
                    }

                    fChannel.close();
                }
                catch ( Exception ex )
                {
                    logger.error( "Error downloading file", ex );
                }
            }
        };
        Response.ResponseBuilder responseBuilder = Response.ok( sOut );
        responseBuilder.header( "Content-Disposition", "attachment; filename=" + filename );
//        responseBuilder.header( "Content-Length", 5668272 );
        responseBuilder.header( "Content-Length", 241777);
        return responseBuilder.build();
    }
}

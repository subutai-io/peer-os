package io.subutai.core.test.dp;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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


    private interface Logging
    {
        void logCounter( Integer integer );
    }

    public static class OpenTrustManager implements X509TrustManager
    {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public static void apply(HttpsURLConnection conn)
                throws NoSuchAlgorithmException, KeyManagementException
        {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { new OpenTrustManager() },
                    new java.security.SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());
        }
    }


    @GET
    @Path( "/books/pdf" )
    @Produces( "application/text" )
    public Response getPdf()
    {
        final Logging logging = new Logging()
        {
            @Override
            public void logCounter( final Integer integer )
            {
                logger.info( String.valueOf( integer ) );
            }
        };
        StreamingOutput sOut = new StreamingOutput()
        {
            public void write( final OutputStream os )
            {
                Integer counter = 0;
                OutputStreamWriter outWriter = new OutputStreamWriter( os );

                Writer writer = new BufferedWriter( outWriter );
                URL website = null;
                try
                {
                    // https://images.unsplash.com/photo-1462733441571-9312d0b53818?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&s=deb060f2ba359f2691166b4278ecdc5b
//                    website = new URL( "https://images.unsplash.com/photo-1462733441571-9312d0b53818?format=auto&auto=compress&dpr=1&crop=entropy&fit=crop&w=1910&h=1273&q=80" );
                    website = new URL( "https://images.unsplash.com/photo-1462733441571-9312d0b53818?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&s=deb060f2ba359f2691166b4278ecdc5b" );
                    URLConnection conn = website.openConnection();
                    if(conn instanceof HttpsURLConnection) {
                        OpenTrustManager.apply((HttpsURLConnection)conn);
                    }
                    conn.connect();

                    ReadableByteChannel rbc = Channels.newChannel( conn.getInputStream() );

                    ByteBuffer byteBuffer = ByteBuffer.allocate( 8192 );
                    FileOutputStream fos = new FileOutputStream( "information.jpg" );
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
//                        Thread.sleep( 2000 );
                    }

                    fChannel.close();
                    logging.logCounter( counter );
                }
                catch ( Exception ex )
                {
                    logger.error("Error downloading file", ex);
                }
            }
        };
        Response.ResponseBuilder responseBuilder = Response.ok( sOut );
        responseBuilder.header( "Content-Disposition", "attachment; filename=information.jpg" );
        responseBuilder.header( "Content-Length", 5668272 );
//        responseBuilder.header( "Content-Length", 241777);
                return responseBuilder.build();
    }
}

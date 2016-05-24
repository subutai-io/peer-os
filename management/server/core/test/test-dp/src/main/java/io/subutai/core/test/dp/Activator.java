/*
 *  @author : Slim Ouertani
 *  @mail : ouertani@gmail.com
 */
package io.subutai.core.test.dp;


import java.io.FileOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.osgi.framework.BundleContext;


/**
 * @author slim ouertani
 */
public class Activator
{
    public void start()
    {

        URL website = null;
        try
        {
            website = new URL("http://bndtools.org/installation.html");

            ReadableByteChannel rbc = Channels.newChannel(website.openStream());

            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
            FileOutputStream fos = new FileOutputStream("information.html");
            FileChannel fChannel = fos.getChannel();

            int bytesRead = rbc.read(byteBuffer);

            while(bytesRead > 0){

                //limit is set to current position and position is set to zero
                byteBuffer.flip();

                while(byteBuffer.hasRemaining()){
                    fChannel.write( byteBuffer );
//                    char ch = (char) byteBuffer.get();
//                    System.out.print(ch);
                }

//                fos.write( bytesRead );
                byteBuffer.clear();
                bytesRead = rbc.read(byteBuffer);
            }

            fChannel.close();
//            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        //        WSClient customWSClient = new AhcWSClient(ahcBuilder.build(), materializer);
    }


    public void stop( BundleContext context ) throws Exception
    {
    }
}

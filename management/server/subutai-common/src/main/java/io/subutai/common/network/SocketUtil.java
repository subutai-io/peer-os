package io.subutai.common.network;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.subutai.common.exception.NetworkException;


public class SocketUtil
{

    private SocketUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static void check( String node, int timeout, int port ) throws NetworkException
    {
        String reason = null;

        boolean success = false;

        try ( Socket s = new Socket() )
        {
            s.setReuseAddress( true );
            SocketAddress sa = new InetSocketAddress( node, port );
            s.connect( sa, timeout * 1000 );
            success = s.isConnected();
        }
        catch ( IOException e )
        {
            if ( "Connection refused".equals( e.getMessage() ) )
            {
                reason = "port " + port + " on " + node + " is closed.";
            }
            if ( e instanceof UnknownHostException )
            {
                reason = "node " + node + " is unresolved.";
            }
            if ( e instanceof SocketTimeoutException )
            {
                reason = "timeout while attempting to reach node " + node + " on port " + port;
            }
        }

        if ( !success )
        {
            throw new NetworkException( "Port " + port + " on " + node + " is not reachable; reason: " + reason );
        }
    }
}

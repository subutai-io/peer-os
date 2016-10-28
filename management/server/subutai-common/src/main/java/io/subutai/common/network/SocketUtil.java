package io.subutai.common.network;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.exception.NetworkException;


public class SocketUtil
{
    protected static final Logger LOG = LoggerFactory.getLogger( SocketUtil.class );


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
        catch ( SocketTimeoutException e )
        {
            LOG.warn( e.getMessage() );

            reason = "timeout while attempting to reach node " + node + " on port " + port;
        }
        catch ( UnknownHostException e )
        {
            LOG.warn( e.getMessage() );

            reason = "node " + node + " is unresolved.";
        }
        catch ( IOException e )
        {
            LOG.warn( e.getMessage() );

            if ( "Connection refused".equals( e.getMessage() ) )
            {
                reason = "port " + port + " on " + node + " is closed.";
            }
            else
            {
                reason = e.getMessage();
            }
        }

        if ( !success )
        {
            throw new NetworkException( "Port " + port + " on " + node + " is not reachable; reason: " + reason );
        }
    }
}

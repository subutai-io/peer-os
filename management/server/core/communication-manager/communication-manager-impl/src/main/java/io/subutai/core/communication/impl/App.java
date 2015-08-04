package io.subutai.core.communication.impl;


import java.net.URL;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.communication.api.SecurityMaterials;


/**
 * Hello world!
 */
public class App
{
    final static String REST_URI = "https://172.16.193.109:444/";

    private static final Logger log = LoggerFactory.getLogger( App.class );
    // the keystore (with one key) we'll use to make the connection with the
    // broker
    private final static String KEYSTORE_LOCATION = "src/main/resources/server.jks";
    private final static String KEYSTORE_PASS = "secret";

    // the truststore we use for our server. This keystore should contain all the keys
    // that are allowed to make a connection to the server
    private final static String TRUSTSTORE_LOCATION = "src/main/resources/truststore.jks";
    private final static String TRUSTSTORE_PASS = "q1wqgzk";
/*
    static
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance( "TLS" );
            sslContext
                    .init( null, new TrustManager[] { new SubutaiTrustManager( TRUSTSTORE_LOCATION, TRUSTSTORE_PASS ) },
                            null );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "Unable to initialise SSL context", e );
        }
        catch ( KeyManagementException e )
        {
            throw new RuntimeException( "Unable to initialise SSL context", e );
        }
    }*/

/*
    private SSLSocketFactory getFactory( File pKeyFile, String pKeyPassword )
            throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException,
            UnrecoverableKeyException, KeyManagementException
    {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( "SunX509" );
        KeyStore keyStore = KeyStore.getInstance( "PKCS12" );

        InputStream keyInput = new FileInputStream( pKeyFile );
        keyStore.load( keyInput, pKeyPassword.toCharArray() );
        keyInput.close();

        keyManagerFactory.init( keyStore, pKeyPassword.toCharArray() );

        SSLContext context = SSLContext.getInstance( "TLS" );
        context.init( keyManagerFactory.getKeyManagers(), null, new SecureRandom() );

        return context.getSocketFactory();
    }
*/



    /**
     * Simple starter for a jetty HTTPS server.
     */
    public static void main( String[] args ) throws Exception
    {

        //        ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        //        SslContextFactory sslContextFactory = ( SslContextFactory ) context.getBean( "sslContextFactory" );
        //
        //        clientBuilder.sslContext( getSSLContext( new File(
        //
        // "/home/tzhamakeev/projects/x509/src/main/resources/1ecac2d4c0a60601c278927259e7bfb153d69837"
        //                                + ".p12" ),
        //                "q1wqgzk" ) );

        URL url = new URL( REST_URI + "/ws/peer" );
        //        HttpsURLConnection con = ( HttpsURLConnection ) url.openConnection();
        //
        //        if ( con instanceof HttpsURLConnection )
        //        {
        //            log.info( "Connection is HTTPS" );
        //            //            SSLSocketFactory socketFactory = getSSLContext( new File(
        //            //
        //            // "/home/tzhamakeev/projects/x509/src/main/resources/1ecac2d4c0a60601c278927259e7bfb153d69837"
        //            //                            + ".p12" ), "q1wqgzk" ).getSocketFactory();
        // SSLSocketFactory
        //            SSLSocketFactory socketFactory =
        //                    getSSLContext( new File( "/home/tzhamakeev/projects/x509/src/main/resources/alice.jks" ),
        //                            "jks123" ).getSocketFactory();
        //            ( ( HttpsURLConnection ) con ).setSSLSocketFactory( socketFactory );
        //        }
        //
        //
        //        // Create all-trusting host name verifier
        //        HostnameVerifier allHostsValid = new HostnameVerifier()
        //        {
        //            public boolean verify( String hostname, SSLSession session )
        //            {
        //                System.out.println( "------------------>" + hostname );
        //                return true;
        //            }
        //        };
        //
        //        // Install the all-trusting host verifier
        //        HttpsURLConnection.setDefaultHostnameVerifier( allHostsValid );
        //
        //        // Send the request.
        //        con.connect();
        //        InputStreamReader in = new InputStreamReader( ( InputStream ) con.getContent() );
        //
        //        in.close();


        //        KeyStore keyStore = loadKeyStore( new File(
        // "/home/tzhamakeev/projects/x509/src/main/resources/alice.jks" ),
        //                "jks123".toCharArray() );
        //        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial( keyStore, "jks123".toCharArray() )
        //                                           .loadTrustMaterial( new TrustSelfSignedStrategy() ).build();
        //
        //
        //        SSLConnectionSocketFactory socketFactory =
        //                new SSLConnectionSocketFactory( sslContext, NoopHostnameVerifier.INSTANCE );
        //
        //        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory( socketFactory ).build();


        PasswordCallback keyStorePasswordCallback = new PasswordCallback( "Please enter key store password:", true );
        PasswordCallback privateKeyPasswordCallback =
                new PasswordCallback( "Please enter GPG private key password:", true );


//        new ConsoleCallbackHandler( "pkcs123" ).handle( new Callback[] { keyStorePasswordCallback } );
//        new ConsoleCallbackHandler( "abc123" ).handle( new Callback[] { privateKeyPasswordCallback } );


        System.out.println( keyStorePasswordCallback.getPassword().length );
        SecurityMaterials securityMaterials =
                new FileSystemSecurityMaterials( "/home/tzhamakeev/projects/x509/src/main/resources/keys", "1ecac2d4c0a60601c278927259e7bfb153d69837",
                        "8120aba66000a01bd8e3020f202bc7d1835422f6", "pkcs12", keyStorePasswordCallback, privateKeyPasswordCallback );

        String originalData = "This is an original data.";

        SubutaiHttpClient client = new SubutaiHttpClient( REST_URI + "/ws/peer", securityMaterials, true );
        log.debug( client.run( originalData ) );

        System.out.println( "Finished." );
        System.exit( 0 );

    }

}
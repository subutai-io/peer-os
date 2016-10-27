package io.subutai.core.hubmanager.impl.adapter;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpStatus;

import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.json.JsonUtil;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;
import io.subutai.hub.share.pgp.message.PGPMessenger;


class HttpClient
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String HUB_ADDRESS = "https://hub.subut.ai:444";

    private final PGPMessenger messenger;

    private static KeyStore peerKeyStore;

    private final String PEER_KEY_FINGERPRINT;


    private synchronized KeyStore getPeerKeyStore() throws HubManagerException
    {
        if ( peerKeyStore == null )
        {
            peerKeyStore = loadKeyStore();
        }

        return peerKeyStore;
    }


    HttpClient( SecurityManager securityManager ) throws HubManagerException
    {
        try
        {
            PGPPrivateKey senderKey = securityManager.getKeyManager().getPrivateKey( null );

            PGPPublicKey receiverKey = PGPKeyHelper.readPublicKey( Common.H_PUB_KEY );

            messenger = new PGPMessenger( senderKey, receiverKey );

            PEER_KEY_FINGERPRINT = PGPKeyHelper.getFingerprint( securityManager.getKeyManager().getPublicKey( null ) );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    String doGet( String path )
    {
        try
        {
            Response response = getWebClient( path ).get();

            return handleResponse( response );
        }
        catch ( Exception e )
        {
            log.error( "Error to execute request: ", e );

            return null;
        }
    }


    String doDelete( String path )
    {
        try
        {
            Response response = getWebClient( path ).delete();

            return handleResponse( response );
        }
        catch ( Exception e )
        {
            log.error( "Error to execute request: ", e );

            return null;
        }
    }


    String doPost( String path, String body )
    {
        try
        {
            byte[] cborData = JsonUtil.toCbor( body );

            byte[] encryptedData = messenger.produce( cborData );

            Response response = getWebClient( path ).post( encryptedData );

            return handleResponse( response );
        }
        catch ( Exception e )
        {
            log.error( "Error to execute request: ", e );

            return null;
        }
    }


    private String handleResponse( Response response ) throws IOException, PGPException
    {
        if ( response.getStatus() != HttpStatus.SC_OK && response.getStatus() != HttpStatus.SC_NO_CONTENT )
        {
            String content = response.readEntity( String.class );

            log.error( "HTTP {}: {}", response.getStatus(), StringUtils.abbreviate( content, 250 ) );

            return null;
        }

        if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
        {
            return "";
        }

        byte[] encryptedContent = readContent( response );

        byte[] plainContent = messenger.consume( encryptedContent );

        return JsonUtil.fromCbor( plainContent, String.class );
    }


    private byte[] readContent( Response response ) throws IOException
    {
        if ( response.getEntity() == null )
        {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        InputStream is = ( InputStream ) response.getEntity();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IOUtils.copy( is, bos );

        return bos.toByteArray();
    }


    private KeyStore loadKeyStore() throws HubManagerException
    {
        KeyStoreTool keyStoreTool = new KeyStoreTool();

        try
        {
            return keyStoreTool.createPeerCertKeystore( Common.PEER_CERT_ALIAS, PEER_KEY_FINGERPRINT );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private WebClient getWebClient( String path ) throws HubManagerException
    {
        WebClient client = WebClient.create( HUB_ADDRESS + path );

        fixAsyncHttp( client );

        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        httpConduit.setClient( getHTTPClientPolicy() );

        httpConduit.setTlsClientParameters( getTLSClientParameters() );

        return client;
    }


    // A client certificate is not provided in SSL context if async connection is used.
    // See details: #311 - Registration failure due to inability to find fingerprint.
    private void fixAsyncHttp( WebClient client )
    {
        Map<String, Object> requestContext = WebClient.getConfig( client ).getRequestContext();

        requestContext.put( "use.async.http.conduit", Boolean.FALSE );
    }


    private TLSClientParameters getTLSClientParameters() throws HubManagerException
    {
        try
        {
            TLSClientParameters tlsClientParameters = new TLSClientParameters();

            tlsClientParameters.setDisableCNCheck( true );

            tlsClientParameters.setTrustManagers( getTrustManagers() );

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );

            keyManagerFactory.init( getPeerKeyStore(), SecuritySettings.KEYSTORE_PX1_PSW.toCharArray() );

            tlsClientParameters.setKeyManagers( keyManagerFactory.getKeyManagers() );

            return tlsClientParameters;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private HTTPClientPolicy getHTTPClientPolicy()
    {
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

        httpClientPolicy.setConnectionTimeout( 1000 * 60L );

        httpClientPolicy.setReceiveTimeout( 1000 * 60 * 5L );

        httpClientPolicy.setMaxRetransmits( 3 );

        return httpClientPolicy;
    }


    private TrustManager[] getTrustManagers()
    {
        X509TrustManager tm = new X509TrustManager()
        {
            @Override
            public void checkClientTrusted( X509Certificate[] x509Certificates, String s ) throws CertificateException
            {
            }


            @Override
            public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException
            {
            }


            @Override
            public X509Certificate[] getAcceptedIssuers()
            {
                return new X509Certificate[0];
            }
        };

        return new TrustManager[] { tm };
    }
}

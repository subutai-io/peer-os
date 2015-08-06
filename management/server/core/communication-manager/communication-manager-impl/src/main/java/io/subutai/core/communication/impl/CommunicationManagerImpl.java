package io.subutai.core.communication.impl;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FilenameUtils;

import io.subutai.common.callback.ConsoleCallbackHandler;
import io.subutai.common.pgp.key.PGPKeyHelper;
import io.subutai.common.pgp.message.PGPMessenger;
import io.subutai.core.communication.api.CommunicationException;
import io.subutai.core.communication.api.CommunicationManager;
import io.subutai.core.communication.api.Response;
import io.subutai.core.communication.api.SecurityMaterials;


/**
 * CommunicationManager implementation
 */
public class CommunicationManagerImpl implements CommunicationManager
{
    final static String REST_URI = "https://172.16.193.109:444/";

    private static final Logger log = LoggerFactory.getLogger( CommunicationManagerImpl.class );


    private BundleContext bundleContext;


    public void setBundleContext( final BundleContext bundleContext )
    {
        this.bundleContext = bundleContext;
    }


    @Override
    public String post( URI uri, String recipientKeyId, String data ) throws CommunicationException
    {

        PasswordCallback keyStorePasswordCallback = new PasswordCallback( "Please enter key store password:", true );
        PasswordCallback privateKeyPasswordCallback =
                new PasswordCallback( "Please enter GPG private key password:", true );

        String senderKeyId = getSender();

        log.debug( String.format( "Sender: %s. Recipient:%s", senderKeyId, recipientKeyId ) );

        SecurityMaterials securityMaterials =
                new FileSystemSecurityMaterials( "/root/keys", /*senderKeyId, recipientKeyId,*/
                        "416924b9e3ca50d43286e1c32b91c22bdf764422", "8120aba66000a01bd8e3020f202bc7d1835422f6", "PKCS12",
                        keyStorePasswordCallback, privateKeyPasswordCallback );

        try
        {
            new ConsoleCallbackHandler( "jks123" ).handle( new Callback[] { keyStorePasswordCallback } );
            new ConsoleCallbackHandler( "abc123" ).handle( new Callback[] { privateKeyPasswordCallback } );

            PGPPrivateKey aliceSecretKey =
                    PGPKeyHelper.readPrivateKey( "/root/keys/alice.secret.gpg", privateKeyPasswordCallback );
            PGPPublicKey alicePublicKey = PGPKeyHelper.readPublicKey( "/root/keys/alice.public.gpg" );
            PGPPrivateKey bobSecretKey = PGPKeyHelper.readPrivateKey( "/root/keys/bobby.secret.gpg", "abc123" );
            PGPPublicKey bobPublicKey = PGPKeyHelper.readPublicKey( "/root/keys/bobby.public.gpg" );


            PGPMessenger pgpMessenger = new PGPMessenger( aliceSecretKey, bobPublicKey );

            byte[] encryptedData = pgpMessenger.produce( data.getBytes( "UTF-8" ) );

            log.debug( new String( encryptedData ) );

            pgpMessenger = new PGPMessenger( bobSecretKey, alicePublicKey );

            byte[] result = pgpMessenger.consume( encryptedData );

            System.out.println( new String( result ) );
        }
        catch ( PGPException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( UnsupportedCallbackException e )
        {
            e.printStackTrace();
        }
        String result = "";
        try
        {
            new ConsoleCallbackHandler( "q1wqgzk" ).handle( new Callback[] { keyStorePasswordCallback } );
            new ConsoleCallbackHandler( "abc123" ).handle( new Callback[] { privateKeyPasswordCallback } );

            result = new HttpsClient().post( uri.toString(), data, securityMaterials, false );

            //            keyStorePasswordCallback.clearPassword();
            //            privateKeyPasswordCallback.clearPassword();
        }
        catch ( Exception e )
        {
            log.error( e.toString(), e );
            throw new CommunicationException( e.toString(), e );
        }

        return result;
    }


    @Override
    public String getSender()
    {
        Enumeration<URL> keys = bundleContext.getBundle().findEntries( "/keys", "*.secret.gpg", true );

        List<String> result = new ArrayList<>();
        String secretKey = null;
        while ( keys.hasMoreElements() )
        {
            String fullName = keys.nextElement().getFile();
            String file = ( FilenameUtils.getBaseName( fullName ).split( "\\." ) )[0];
            if ( fullName.contains( "secret" ) )
            {
                secretKey = file;
            }
        }

        if ( secretKey == null )
        {
            return null;
        }
        else
        {
            return secretKey;
        }
    }


    @Override
    public List<String> getRecipients()
    {
        log.debug( "Get recipients invoked..." );

        Enumeration<URL> keys = bundleContext.getBundle().findEntries( "/keys", "*.gpg", true );

        List<String> result = new ArrayList<>();
        String secretKey = null;
        while ( keys.hasMoreElements() )
        {
            String fullName = keys.nextElement().getFile();
            String file = ( FilenameUtils.getBaseName( fullName ).split( "\\." ) )[0];
            if ( fullName.contains( "secret" ) )
            {
                secretKey = file;
            }
            else
            {
                result.add( file );
            }
        }

        if ( secretKey != null )
        {
            result.remove( secretKey );
        }
        return result;
    }
}
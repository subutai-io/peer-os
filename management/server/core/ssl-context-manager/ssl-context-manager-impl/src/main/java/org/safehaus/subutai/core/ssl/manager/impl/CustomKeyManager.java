package org.safehaus.subutai.core.ssl.manager.impl;


import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.X509KeyManager;

import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;
import org.safehaus.subutai.common.settings.SecuritySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 3/3/15.
 */
public class CustomKeyManager implements X509KeyManager
{
    private static final Logger log = LoggerFactory.getLogger( CustomKeyManager.class );

    private KeyStoreManager keyStoreManager;

    private KeyStoreData keyStoreData;


    public CustomKeyManager()
    {
        keyStoreManager = new KeyStoreManager();
        keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
    }


    @Override
    public String[] getClientAliases( final String keyType, final Principal[] issuers )
    {
        log.warn( String.format( "%s %s", keyType, Arrays.asList( issuers ) ) );
        log.warn( "################ getClientAliases" );
        return getAliases( issuers );
    }


    @Override
    public String chooseClientAlias( final String[] keyType, final Principal[] issuers, final Socket socket )
    {
        String result = getAliases( issuers )[0];
        log.warn( "############ chooseClientAlias" );
        return result;
    }


    @Override
    public String[] getServerAliases( final String keyType, final Principal[] issuers )
    {
        log.warn( "############## getServerAliases" );
        return getAliases( issuers );
    }


    @Override
    public String chooseServerAlias( final String keyType, final Principal[] issuers, final Socket socket )
    {
        log.warn( "################# chooseServerAlias" );
        return getAliases( issuers )[0];
    }


    @Override
    public X509Certificate[] getCertificateChain( final String alias )
    {
        log.warn( "################## getCertificateChain" );

        try
        {
            KeyStore keyStore = keyStoreManager.load( keyStoreData );
            Certificate chain[] = keyStore.getCertificateChain( alias );
            List<X509Certificate> xchain = new ArrayList<>();
            for ( final Certificate certificate : chain )
            {
                if ( certificate instanceof X509Certificate )
                {
                    xchain.add( ( X509Certificate ) certificate );
                }
            }
            return xchain.toArray( new X509Certificate[xchain.size()] );
        }
        catch ( KeyStoreException e )
        {
            log.warn( "no keyStore chain is found.", e );
            return new X509Certificate[0];
        }
        //        return new X509Certificate[0];
    }


    @Override
    public PrivateKey getPrivateKey( final String alias )
    {
        log.warn( "################## getPrivateKey" );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );
        KeyStore.PrivateKeyEntry entry = null;
        try
        {
            KeyStore.Entry newEntry = keyStore.getEntry( alias,
                    new KeyStore.PasswordProtection( SecuritySettings.KEYSTORE_PX1_PSW.toCharArray() ) );
            if ( !( newEntry instanceof KeyStore.PrivateKeyEntry ) )
            {
                // unexpected type of entry
                return null;
            }
            entry = ( KeyStore.PrivateKeyEntry ) newEntry;
        }
        catch ( NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e )
        {
            log.error( "Error getting keyStore entry", e );
        }
        //        keyStore.get
        return entry == null ? null : entry.getPrivateKey();
        //        return null;
    }


    // make a Set out of the array
    private Set<Principal> getIssuerSet( Principal[] issuers )
    {
        if ( ( issuers != null ) && ( issuers.length != 0 ) )
        {
            return new HashSet<>( Arrays.asList( issuers ) );
        }
        else
        {
            return null;
        }
    }


    private String[] getAliases( final Principal[] issuers )
    {
        Set<Principal> issuerSet = getIssuerSet( issuers );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );
        List<String> result = new ArrayList<>();
        try
        {
            for ( Enumeration<String> enumeration = keyStore.aliases(); enumeration.hasMoreElements(); )
            {
                String alias = enumeration.nextElement();
                Certificate[] chain = keyStore.getCertificateChain( alias );

                boolean compatible = true;
                for ( final Certificate certificate : chain )
                {
                    if ( ( certificate instanceof X509Certificate ) )
                    {
                        compatible = false;
                    }
                }
                if ( !compatible )
                {
                    continue;
                }

                if ( issuerSet != null )
                {
                    boolean found = false;
                    for ( Certificate cert : chain )
                    {
                        X509Certificate xcert = ( X509Certificate ) cert;
                        if ( issuerSet.contains( xcert.getIssuerX500Principal() ) )
                        {
                            found = true;
                            break;
                        }
                    }
                    if ( !found )
                    {
                        log.warn( "Certificate issuer not found." );
                    }
                }
                try
                {
                    X509Certificate cert = ( X509Certificate ) chain[0];
                    cert.checkValidity( new Date() );
                    result.add( alias );
                    break;
                }
                catch ( CertificateException ignore )
                {
                }
            }
        }
        catch ( KeyStoreException e )
        {
            log.error( "Error accessing keyStore aliases.", e );
        }
        return result.toArray( new String[result.size()] );
        //        return null;
    }
}

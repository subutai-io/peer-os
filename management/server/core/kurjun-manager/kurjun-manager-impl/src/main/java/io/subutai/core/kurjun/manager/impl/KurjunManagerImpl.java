package io.subutai.core.kurjun.manager.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.RestUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.manager.api.KurjunManager;
import io.subutai.core.kurjun.manager.api.dao.KurjunDataService;
import io.subutai.core.kurjun.manager.api.model.Kurjun;
import io.subutai.core.kurjun.manager.api.model.KurjunConfig;
import io.subutai.core.kurjun.manager.impl.dao.KurjunDataServiceImpl;
import io.subutai.core.kurjun.manager.impl.model.KurjunConfigEntity;
import io.subutai.core.kurjun.manager.impl.model.KurjunEntity;
import io.subutai.core.kurjun.manager.impl.model.KurjunType;
import io.subutai.core.security.api.SecurityManager;


/**
 *
 */
public class KurjunManagerImpl implements KurjunManager
{
    private static final Logger LOG = LoggerFactory.getLogger( KurjunManagerImpl.class.getName() );

    //**********************************
    private IdentityManager identityManager;
    private SecurityManager securityManager;
    private DaoManager daoManager;

    private KurjunDataService dataService;
    private static Properties properties;
    private static String ownerKey;
    //**********************************


    //****************************************
    public void init()
    {
        dataService = new KurjunDataServiceImpl( daoManager );

        if ( dataService.getAllKurjunData().isEmpty() )
        try
        {

            for ( final String url : SystemSettings.getGlobalKurjunUrls() )
            {
                Kurjun kurjun = new KurjunEntity();

                kurjun.setType( KurjunType.Global.getId() );
                kurjun.setState( false );
                kurjun.setUrl( url );

                dataService.persistKurjunData( kurjun );
            }

            for ( final String url : SystemSettings.getLocalKurjunUrls() )
            {
                Kurjun kurjun = new KurjunEntity();

                kurjun.setType( KurjunType.Local.getId() );
                kurjun.setState( false );
                kurjun.setUrl( url );

                dataService.persistKurjunData( kurjun );
            }
        }
        catch ( ConfigurationException e )
        {
            e.printStackTrace();
        }

        PGPPublicKey key =
                securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                               .getPublicKey();

        String fingerprint = PGPKeyUtil.getFingerprint( key.getFingerprint() );

        try
        {
            ownerKey = PGPKeyUtil.exportAscii( key );
        }
        catch ( PGPException e )
        {
            e.printStackTrace();
        }


        properties = loadProperties();



//        if ( Strings.isNullOrEmpty( getUser( KurjunType.Local.getId(), fingerprint ) ) )
//        {
//            registerUser( KurjunType.Local.getId(), fingerprint );
//        }
//        else
//        {
//            authorizeUser( KurjunType.Local.getId(), fingerprint );
//        }
    }


    //****************************************
    private String getKurjunUrl( int kurjunType, String uri )
    {
        try
        {
            if ( kurjunType == KurjunType.Local.getId() )
            {
                for ( final String s : SystemSettings.getLocalKurjunUrls() )
                {
                    return s + uri;
                }
            }
            else if ( kurjunType == KurjunType.Global.getId() )
            {
                for ( final String s : SystemSettings.getGlobalKurjunUrls() )
                {
                    return s + uri;
                }
            }
            else
            {
                for ( final String s : SystemSettings.getGlobalKurjunUrls() )
                {
                    return s + uri;
                }
            }
        }
        catch ( ConfigurationException e )
        {
            e.printStackTrace();
        }

        //        if ( kurjunType == KurjunType.Local.getId() )
        //        {
        //            return localKurjunURL + uri;
        //        }
        //        else if ( kurjunType == KurjunType.Global.getId() )
        //        {
        //            return globalKurjunURL + uri;
        //        }
        //        else
        //        {
        //            return globalKurjunURL + uri;
        //        }
        return null;
    }


    //****************************************
    @Override
    public String registerUser( int kurjunType, String fingerprint )
    {
        //*****************************************
        String authId = "";

        if ( Strings.isNullOrEmpty( getUser( kurjunType, fingerprint ) ) )
        {
            String url = getKurjunUrl( kurjunType, properties.getProperty( "url.identity.user.add" ) );
            WebClient client = RestUtil.createTrustedWebClient( url );
            client.query( "key", ownerKey );

            Response response = client.post( null );

            if ( response.getStatus() == HttpStatus.SC_OK )
            {
                authId = response.readEntity( String.class );
            }
            else
            {
                return null;
            }
        }


        KurjunConfig config = new KurjunConfigEntity();

        config.setAuthID( authId );
        config.setOwnerFingerprint( fingerprint );
        config.setType( kurjunType );

        dataService.persistKurjunConfig( config );
        //        if ( dataService.getKurjunData( fingerprint ) == null )
        //        {
        //            byte[] signedMessage = null;
        //
        //            Kurjun kurjun = new KurjunEntity();
        //            kurjun.setType( kurjunType );
        //            kurjun.setOwnerFingerprint( fingerprint );
        //            kurjun.setAuthID( authId );
        //            //            kurjun.setSignedMessage(  null );
        //            kurjun.setSignedMessage( signedMessage );
        //            dataService.persistKurjunData( kurjun );
        //        }

        return null;
    }


    //****************************************
    @Override
    public String authorizeUser( int kurjunType, String fingerprint )
    {
        String url = getKurjunUrl( kurjunType, properties.getProperty( "url.identity.user.auth" ) );
        WebClient client = RestUtil.createTrustedWebClient( url );

        byte[] signedMsg = null;

        List<Kurjun> list = dataService.getAllKurjunData();
        for ( final Kurjun kurjun : list )
        {
//            signedMsg = kurjun.getSignedMessage();
            break;
        }

        try
        {
            client.query( "fingerprint", fingerprint );
            client.query( "message", PGPEncryptionUtil.armorByteArrayToString( signedMsg ) );

            Response response = client.post( null );

            if ( response.getStatus() != HttpStatus.SC_OK )
            {
                return null;
            }
        }
        catch ( PGPException e )
        {
            e.printStackTrace();
        }


        return "success";
    }


    //****************************************
    @Override
    public boolean setSystemOwner( int kurjunType, String fingerprint )
    {

        return true;
    }


    //****************************************
    @Override
    public String getSystemOwner( int kurjunType )
    {
        String url = getKurjunUrl( kurjunType, properties.getProperty( "url.identity.user.auth" ) );
        WebClient client = RestUtil.createTrustedWebClient( url );

        return null;
    }


    //****************************************
    @Override
    public String getUser( int kurjunType, String fingerprint )
    {
        String url = getKurjunUrl( kurjunType, properties.getProperty( "url.identity.user.get" ) );
        WebClient client = RestUtil.createTrustedWebClient( url );
        client.query( "fingerprint", fingerprint );

        Response response = client.get();

        if ( response.getStatus() != HttpStatus.SC_OK )
        {
            LOG.error( "Could not get AuthId:" + response.readEntity( String.class ) );
            return null;
        }

        return null;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    private Properties loadProperties()
    {
        Properties configProp = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream( "rest.properties" );
        try
        {
            configProp.load( in );
            return configProp;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return null;
        }
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public KurjunDataService getDataService()
    {
        return dataService;
    }
}

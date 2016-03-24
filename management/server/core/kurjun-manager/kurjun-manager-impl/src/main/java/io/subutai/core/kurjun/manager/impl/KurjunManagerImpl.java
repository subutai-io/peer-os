package io.subutai.core.kurjun.manager.impl;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPublicKeyRing;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.manager.api.KurjunManager;
import io.subutai.core.kurjun.manager.api.model.Kurjun;
import io.subutai.core.kurjun.manager.impl.dao.KurjunDataService;
import io.subutai.core.kurjun.manager.impl.model.KurjunEntity;
import io.subutai.core.kurjun.manager.impl.model.KurjunType;
import io.subutai.core.security.api.SecurityManager;


/**
 *
 */
public class KurjunManagerImpl implements KurjunManager
{
    //**********************************
    private IdentityManager identityManager;
    private SecurityManager securityManager;
    private DaoManager daoManager;
    private KurjunDataService dataService;
    private static Properties properties;
    //**********************************


    //TODO getValues from SystemManager;

    private String localKurjunURL;
    private String globalKurjunURL;


    //****************************************
    //    public void KurjunManagerImpl( /*IdentityManager identityManager, SecurityManager securityManager,*/
    //                                   DaoManager daoManager )
    //    {
    //        this.identityManager = identityManager;
    //        this.securityManager = securityManager;
    //        this.daoManager = daoManager;
    //
    //        dataService = new KurjunDataService( daoManager );
    //    }


    //****************************************
    public void init()
    {
        String fingerprint = securityManager.getKeyManager().getFingerprint( null );
        properties = loadProperties();

        dataService = new KurjunDataService( daoManager );


        if ( Strings.isNullOrEmpty( getUser( KurjunType.Local.getId(), fingerprint ) ) )
        {
            registerUser( KurjunType.Local.getId(), fingerprint );
        }
        else
        {
            authorizeUser( KurjunType.Local.getId(), fingerprint );
        }
    }


    //****************************************
    private String getKurjunUrl( int kurjunType, String uri )
    {
        if ( kurjunType == KurjunType.Local.getId() )
        {
            return localKurjunURL + uri;
        }
        else if ( kurjunType == KurjunType.Global.getId() )
        {
            return globalKurjunURL + uri;
        }
        else
        {
            return globalKurjunURL + uri;
        }
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
            Response response = client.get();

            //TODO get authID from client
            //authId = client Output;
        }


        if ( dataService.getKurjunData( fingerprint ) == null )
        {

            //************* Sign *********************
            String signedMessage = ""; //securityManager.getEncryptionTool().
            //****************************************

            Kurjun kurjun = new KurjunEntity();
            kurjun.setType( kurjunType );
            kurjun.setOwnerFingerprint( fingerprint );
            kurjun.setAuthID( authId );
            kurjun.setSignedMessage( signedMessage );
            dataService.persistKurjunData( kurjun );
        }

        return null;
    }


    //****************************************
    @Override
    public String authorizeUser( int kurjunType, String fingerprint )
    {
        String url = getKurjunUrl( kurjunType, properties.getProperty( "url.identity.user.get" ) );
        WebClient client = RestUtil.createTrustedWebClient( url );

        return null;
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
        System.out.println( "Read all properties from file" );
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
}

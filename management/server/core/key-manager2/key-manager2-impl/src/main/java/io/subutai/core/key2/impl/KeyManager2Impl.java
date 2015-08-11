package io.subutai.core.key2.impl;


import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.util.StringUtil;
import io.subutai.core.key2.api.KeyInfo2;
import io.subutai.core.key2.api.KeyManager2;
import io.subutai.core.key2.api.KeyManagerException;


/**
 * Created by caveman on 21.07.2015.
 */
public class KeyManager2Impl implements KeyManager2
{
    /**
     * Generates a PGP key
     */

    private static final Logger LOG = LoggerFactory.getLogger( KeyManager2Impl.class.getName() );


    @Override
    public KeyInfo2 generateKey( String realName, String email ) throws KeyManagerException
    {

        String output = Commands2.generateKeyCommand( realName, email );
        Set<KeyInfo2> keyInfoSet = this.parseKeysFromOutput( output );
        if ( keyInfoSet.isEmpty() )
        {
            throw new KeyManagerException(
                    String.format( "Could not obtain key info from command output:%n%s", output ) );
        }
        else
        {
            return keyInfoSet.iterator().next();
        }
    }


    @Override
    public String generateCertificate( String keyId ) throws KeyManagerException
    {
        return Commands2.generateCertificateCommand( keyId );
    }


    /**
     * Returns PGP public key
     *
     * @param keyId - id of pgp key
     */
    @Override
    public String readKey( String keyId ) throws KeyManagerException
    {
        return Commands2.readKeyWithId( keyId );
    }


    /**
     * Returns a X.509 self-signed certificate content of a key ( generates it if there is no certificate created with
     * that given key id in the keyring)
     *
     * @param keyId - id of pgp key
     *
     * @return - content of certificate
     */
    @Override
    public String getCertificate( String keyId ) throws KeyManagerException
    {
        return Commands2.getCertificate( keyId );
    }


    /**
     * Returns PGP public key as SSH key
     *
     * @param keyId - id of pgp key
     */
    @Override
    public String readSshKey( String keyId ) throws KeyManagerException
    {
        return Commands2.readKeySshKeyWithId( keyId );
    }


    /**
     * Sign file with specified key
     *
     * @param keyId - id of pgp key which is used to sign
     * @param filePath - full path to file to be signed
     */
    @Override
    public void signFileWithKey( String keyId, String filePath ) throws KeyManagerException
    {
        String output = Commands2.signFile( keyId, filePath );
        if ( output != null )
        {
            LOG.info( "File signed." );
        }
        else
        {
            LOG.info( "File can not be signed." );
        }
    }


    /**
     * Sign key with specified key
     *
     * @param signerKeyId - id of pgp key which is used to sign
     * @param signedKeyId - id of pgp key which is to be signed
     */
    @Override
    public void signKeyWithKey( String signerKeyId, String signedKeyId ) throws KeyManagerException
    {

        String output = Commands2.signKeyWithKey( signerKeyId, signedKeyId );
        if ( output != null )
        {
            LOG.info( "Key signed with key" );
        }
        else
        {
            LOG.info( "Key can not be signed." );
        }
    }


    /**
     * experimental...
     */
    @Override
    public void signKeyWithKey2( String signer, String singee ) throws KeyManagerException
    {
        String output = Commands2.signKeyWithKey2( signer, singee );
        if ( output != null )
        {
            LOG.info( "Key Signed!" );
        }
        else
        {
            LOG.info( "Key can not be signed!" );
        }
    }


    /**
     * Sends key to public revocation server
     *
     * @param keyId - id of pgp key to be sent
     */
    @Override
    public void sendRevocationKeyToPublicKeyServer( String keyId ) throws KeyManagerException
    {
        String output = Commands2.sendKeyToPublicServer( keyId );
        if ( output != null )
        {
            LOG.info( "Key send to public server." );
        }
        else
        {
            LOG.info( "Key can not be send to public server." );
        }
    }


    /**
     * Generates revocation key for pgg key
     *
     * @param keyId - id of pgp key
     */
    @Override
    public void generateRevocationKey( String keyId ) throws KeyManagerException
    {
        String output = Commands2.generateRevocationKey( keyId );
        if ( output != null )
        {
            LOG.info( "Revocation key generated." );
        }
        else
        {
            LOG.info( "Revocation key can not be generated." );
        }
    }


    /**
     * Return key info
     *
     * @param keyId - id of pgp key whose info to return
     *
     * @return - {@code KeyInfo2}
     */
    @Override
    public KeyInfo2 getKey( String keyId ) throws KeyManagerException
    {

        String output = Commands2.listKeyWithID( keyId );
        Set<KeyInfo2> keyInfoSet = this.parseKeysFromOutput( output );
        if ( keyInfoSet != null )
        {
            return keyInfoSet.iterator().next();
        }
        else
        {
            return null;
        }
    }


    /**
     * Returns info of all existing keys
     *
     * @return - set of {@code KeyInfo2}
     */
    @Override
    public Set<KeyInfo2> getKeys() throws KeyManagerException
    {
        String output = Commands2.listAllKeys();
        Set<KeyInfo2> keyInfoSet = this.parseKeysFromOutput( output );
        if ( keyInfoSet != null )
        {
            return keyInfoSet;
        }
        else
        {
            return null;
        }
    }


    /**
     * Deletes a key
     *
     * @param keyId - id of pgp key to delete
     */
    @Override
    public void deleteKey( String keyId ) throws KeyManagerException
    {
        String output = Commands2.deleteKey( keyId );
        if ( output != null )
        {
            LOG.info( "Key deleted." );
        }
        else
        {
            LOG.info( "Key can not be deleted." );
        }
    }


    /**
     * Revokes a key
     *
     * @param keyId - id of pgp key to revoke
     */
    @Override
    public void revokeKey( String keyId ) throws KeyManagerException
    {
        String output = Commands2.revokeKey( keyId );
        if ( output != null )
        {
            LOG.info( "Key revoked." );
        }
        else
        {
            LOG.info( "Key can not be revoked." );
        }
    }


    /**
     * Generates a subKey
     *
     * @param keyId - id of pgp key which sub key to generate
     *
     * @return - returns id of a newly created sub key
     */
    @Override
    public String generateSubKey( String keyId ) throws KeyManagerException
    {

        String output = Commands2.generateSubKey( keyId );
        return output;
    }


    /**
     * Deletes a subKey
     *
     * @param keyId - id of pgp sub key to delete
     */
    @Override
    public void deleteSubKey( String keyId ) throws KeyManagerException
    {
        String output = Commands2.deleteSubKey( keyId );
        if ( output != null )
        {
            LOG.info( "Subkey deleted." );
        }
        else
        {
            LOG.info( "Subkey can not be deleted." );
        }
    }


    /**
     * Revokes a subKey
     *
     * @param keyId - id of pgp sub key to revoke
     */
    @Override
    public void revokeSubKey( String keyId ) throws KeyManagerException
    {
        String output = Commands2.revokeSubKey( keyId );
        if ( output != null )
        {
            LOG.info( "Revoked subkey." );
        }
        else
        {
            LOG.info( "Subkey can not be revoked." );
        }
    }


    private Set<KeyInfo2> parseKeysFromOutput( String output )
    {
        Set<KeyInfo2> keyInfoSet = Sets.newHashSet();

        String eol = System.getProperty( "line.separator" );
        List<String> lines = StringUtil.splitString( output, eol );
        int i = 0;
        Pattern keyPattern =
                Pattern.compile( "\\s*(\\w+)\\s+(\\w+(?:\\s*:\\s*\\w+)*)\\s+(\\w+(?:\\s+\\w+)*)\\s+(.+@.+)" );
        for ( String line : lines )
        {
            //skip headers
            if ( i > 1 )
            {
                Matcher keyMatcher = keyPattern.matcher( line );
                if ( keyMatcher.find() )
                {
                    String pubKeyId = keyMatcher.group( 1 );
                    String subKeyIdsString = keyMatcher.group( 2 );
                    String realName = keyMatcher.group( 3 );
                    String email = keyMatcher.group( 4 );
                    //parse sub keys to set
                    Set<String> subKeyIds = parseSubKeyIds( subKeyIdsString );

                    keyInfoSet.add( new KeyInfoImpl2( realName, email, pubKeyId, subKeyIds ) );
                }
            }
            i++;
        }

        return keyInfoSet;
    }


    private Set<String> parseSubKeyIds( String subKeyIdsString )
    {
        Set<String> subKeys = Sets.newHashSet();

        List<String> subKeyIds = StringUtil.splitString( subKeyIdsString, ":" );
        for ( String subKeyId : subKeyIds )
        {
            subKeys.add( subKeyId );
        }
        return subKeys;
    }
}

package org.safehaus.subutai.core.key.impl;


import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManager;
import org.safehaus.subutai.core.key.api.KeyManagerException;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Implementation of KeyManager.
 */
public class KeyManagerImpl implements KeyManager
{
    protected Commands commands = new Commands();
    protected CommandUtil commandUtil = new CommandUtil();


    protected String execute( RequestBuilder requestBuilder, Host host ) throws KeyManagerException
    {
        try
        {
            CommandResult result = commandUtil.execute( requestBuilder, host );
            return result.getStdOut();
        }
        catch ( CommandException e )
        {
            throw new KeyManagerException( e );
        }
    }


    private Set<KeyInfo> parseKeysFromOutput( String output )
    {
        Set<KeyInfo> keyInfoSet = Sets.newHashSet();

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

                    keyInfoSet.add( new KeyInfoImpl( realName, email, pubKeyId, subKeyIds ) );
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


    @Override
    public KeyInfo generateKey( final Host host, final String realName, final String email ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( realName ), "Invalid real name" );
        Preconditions.checkArgument( EmailValidator.getInstance().isValid( email ), "Invalid email" );

        String output = execute( commands.getGenerateKeyCommand( realName, email ), host );

        Set<KeyInfo> keyInfoSet = parseKeysFromOutput( output );
        if ( !keyInfoSet.isEmpty() )
        {
            return keyInfoSet.iterator().next();
        }
        else
        {
            throw new KeyManagerException(
                    String.format( "Could not obtain key info from command output:%n%s", output ) );
        }
    }


    @Override
    public String readKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        return execute( commands.getReadKeyCommand( keyId ), host );
    }


    @Override
    public String readSshKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        return execute( commands.getReadSshKeyCommand( keyId ), host );
    }


    @Override
    public void signFileWithKey( final Host host, final String keyId, final String filePath ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( filePath ), "Invalid file path" );

        execute( commands.getSignCommand( keyId, filePath ), host );
    }


    @Override
    public void signKeyWithKey( final Host host, final String signerKeyId, final String signedKeyId )
            throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( signerKeyId ), "Invalid signer key id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( signedKeyId ), "Invalid signed key id" );

        execute( commands.getSignKeyCommand( signerKeyId, signedKeyId ), host );
    }


    @Override
    public void sendRevocationKeyToPublicKeyServer( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getSendKeyCommand( keyId ), host );
    }


    @Override
    public KeyInfo getKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        String output = execute( commands.getListKeyCommand( keyId ), host );

        Set<KeyInfo> keyInfoSet = parseKeysFromOutput( output );
        if ( !keyInfoSet.isEmpty() )
        {
            return keyInfoSet.iterator().next();
        }
        else
        {
            throw new KeyManagerException( String.format( "Key %s not found in command output:%n%s", keyId, output ) );
        }
    }


    @Override
    public Set<KeyInfo> getKeys( final Host host ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );

        String output = execute( commands.getListKeysCommand(), host );

        return parseKeysFromOutput( output );
    }


    @Override
    public void deleteKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getDeleteKeyCommand( keyId ), host );
    }


    @Override
    public void generateRevocationKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getGenerateRevocationKeyCommand( keyId ), host );
    }


    @Override
    public void revokeKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getRevokeKeyCommand( keyId ), host );
    }


    @Override
    public String generateSubKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        String output = execute( commands.getGenerateSubKeyCommand( keyId ), host );

        String regex = "(\\w+)$";
        Pattern pattern = Pattern.compile( regex );
        Matcher m = pattern.matcher( output );

        if ( m.find() )
        {
            return m.group( 1 );
        }
        else
        {
            throw new KeyManagerException( String.format( "Sub key id not found in command output:%n%s", output ) );
        }
    }


    @Override
    public void deleteSubKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getDeleteSubKeyCommand( keyId ), host );
    }


    @Override
    public void revokeSubKey( final Host host, final String keyId ) throws KeyManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getRevokeSubKeyCommand( keyId ), host );
    }
}

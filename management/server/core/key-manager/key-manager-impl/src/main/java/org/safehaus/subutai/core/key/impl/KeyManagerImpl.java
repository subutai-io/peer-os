package org.safehaus.subutai.core.key.impl;


import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManager;
import org.safehaus.subutai.core.key.api.KeyManagerException;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Implementation of KeyManager.
 */
public class KeyManagerImpl implements KeyManager
{
    private final PeerManager peerManager;
    protected Commands commands = new Commands();
    protected CommandUtil commandUtil = new CommandUtil();


    public KeyManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    protected ManagementHost getManagementHost() throws KeyManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost();
        }
        catch ( HostNotFoundException e )
        {
            throw new KeyManagerException( e );
        }
    }


    protected String execute( RequestBuilder requestBuilder ) throws KeyManagerException
    {
        try
        {
            CommandResult result = commandUtil.execute( requestBuilder, getManagementHost() );
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
        Pattern keyPattern = Pattern.compile( "\\s*(\\w+)\\s*(\\w+(?:\\s*:\\s*\\w+)*)\\s*'(.+)'\\s*(.+)" );
        for ( String line : lines )
        {
            //skip headers
            if ( i > 0 )
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
    public KeyInfo generateKey( final String realName, final String email ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( realName ), "Invalid real name" );
        Preconditions.checkArgument( EmailValidator.getInstance().isValid( email ), "Invalid email" );

        String output = execute( commands.getGenerateKeyCommand( realName, email ) );

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
    public void exportSshKey( final String keyId, final String exportPath ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( exportPath ), "Invalid export path" );

        execute( commands.getExportSshKeyCommand( keyId, exportPath ) );
    }


    @Override
    public void signFileWithKey( final String keyId, final String filePath ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( filePath ), "Invalid file path" );

        execute( commands.getSignCommand( keyId, filePath ) );
    }


    @Override
    public void sendKeyToHub( final String keyId ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        execute( commands.getSendKeyCommand( keyId ) );
    }


    @Override
    public KeyInfo getKey( final String keyId ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );

        String output = execute( commands.getListKeyCommand( keyId ) );

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
    public Set<KeyInfo> getKeys() throws KeyManagerException
    {
        String output = execute( commands.getListKeysCommand() );

        return parseKeysFromOutput( output );
    }
}

package org.safehaus.subutai.core.key.impl;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManager;
import org.safehaus.subutai.core.key.api.KeyManagerException;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

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


    @Override
    public KeyInfo generateKey( final String realName, final String email ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( realName ), "Invalid real name" );
        Preconditions.checkArgument( EmailValidator.getInstance().isValid( email ), "Invalid email" );

        String output = execute( commands.getGenerateKeyCommand( realName, email ) );

        Pattern keyPattern = Pattern.compile(
                "pub id\\s*:\\s*(\\w+)\\s*sub ids\\s*:\\s*(\\w+(?:\\s*,\\s*\\w+)*)\\s*uid\\s*:\\s*(\\w+)"
                        + "\\s*mail\\s*:\\s*(\\w+)" );

        Matcher keyMatcher = keyPattern.matcher( output );

        if ( keyMatcher.find() )
        {
            //pub id
            String pubId = keyMatcher.group( 1 );
            //sub ids
            String subIds = keyMatcher.group( 2 );
            //uid
            String userId = keyMatcher.group( 3 );
            //email
            String eMail = keyMatcher.group( 4 );
            //parse sub ids
            String[] subIdsArray = StringUtils.split( subIds, ',' );
            Set<String> subIdsSet = Sets.newHashSet();
            for ( String subId : subIdsArray )
            {
                subIdsSet.add( subId.trim() );
            }

            return new KeyInfoImpl( userId, eMail, pubId, subIdsSet );
        }
        else
        {
            throw new KeyManagerException( String.format( "Could not parse output of command:%n%s", output ) );
        }
    }


    @Override
    public void exportSshKey( final String keyId, final String pathToExportedSshKey ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( pathToExportedSshKey ), "Invalid export path" );

        execute( commands.getExportSshKeyCommand( keyId, pathToExportedSshKey ) );
    }


    @Override
    public void signFileWithKey( final String keyId, final String pathToFileToBeSigned ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( pathToFileToBeSigned ), "Invalid file path" );

        execute( commands.getSignCommand( keyId, pathToFileToBeSigned ) );
    }


    @Override
    public void sendKeyToUrl( final String keyId, final String url ) throws KeyManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keyId ), "Invalid key id" );
        Preconditions.checkArgument( UrlValidator.getInstance().isValid( url ), "Invalid url" );

        execute( commands.getSendKeyCommand( keyId, url ) );
    }
}

package io.subutai.common.security;


import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.util.CollectionUtil;


public class SshKeys
{
    @JsonProperty( "keys" )
    private Set<SshKey> keys = Sets.newConcurrentHashSet();


    public SshKeys( @JsonProperty( "keys" ) final Set<SshKey> keys )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( keys ) );

        this.keys = keys;
    }


    public SshKeys()
    {

    }


    public void addStringKeys( Set<String> stringSshKeys )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( stringSshKeys ) );

        for ( String stringSshKey : stringSshKeys )
        {
            addKey( new SshKey( null, SshEncryptionType.parseTypeFromKey( stringSshKey ), stringSshKey ) );
        }
    }


    public Set<SshKey> getKeys()
    {
        return keys;
    }


    public void addKey( SshKey sshKey )
    {
        Preconditions.checkNotNull( sshKey );

        this.keys.add( sshKey );
    }


    public void addKeys( final Set<SshKey> keys )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( keys ) );

        for ( SshKey sshKey : keys )
        {
            addKey( sshKey );
        }
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtil.isCollectionEmpty( keys );
    }
}

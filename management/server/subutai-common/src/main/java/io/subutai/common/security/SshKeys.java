package io.subutai.common.security;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;


public class SshKeys
{
    @JsonProperty( "keys" )
    private Set<SshKey> keys = new HashSet<>();


    public SshKeys( @JsonProperty( "keys" ) final Set<SshKey> keys )
    {
        this.keys = keys;
    }


    public SshKeys()
    {

    }


    public Set<SshKey> getKeys()
    {
        return keys;
    }


    public void addKey( SshKey sshKey )
    {
        this.keys.add( sshKey );
    }


    public void addKeys( final Set<SshKey> keys )
    {
        for ( SshKey sshKey : keys )
        {
            addKey( sshKey );
        }
    }
}

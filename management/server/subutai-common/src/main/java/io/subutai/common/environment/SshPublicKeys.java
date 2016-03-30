package io.subutai.common.environment;


import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

import io.subutai.common.util.CollectionUtil;


public class SshPublicKeys
{
    @Expose
    @JsonProperty( "sshPublicKeys" )
    private Set<String> sshPublicKeys = Sets.newHashSet();


    public SshPublicKeys( @JsonProperty( "sshPublicKeys" ) final Set<String> sshPublicKeys )
    {
        this.sshPublicKeys = sshPublicKeys;
    }


    public SshPublicKeys()
    {
    }


    public void addSshPublicKey( final String sshPubicKey )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPubicKey ), "Invalid ssh key" );

        sshPublicKeys.add( sshPubicKey );
    }


    public Set<String> getSshPublicKeys()
    {
        return sshPublicKeys;
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtil.isCollectionEmpty( sshPublicKeys );
    }
}

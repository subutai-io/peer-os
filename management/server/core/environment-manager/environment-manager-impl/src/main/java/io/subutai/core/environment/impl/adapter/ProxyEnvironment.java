package io.subutai.core.environment.impl.adapter;


import io.subutai.core.environment.impl.entity.EnvironmentImpl;


class ProxyEnvironment extends EnvironmentImpl
{
    ProxyEnvironment( String name, String subnetCidr, String sshKey, Long userId, String peerId )
    {
        super( name, subnetCidr, sshKey, userId, peerId );
    }


    public void setId( String id )
    {
        environmentId = id;
    }

}

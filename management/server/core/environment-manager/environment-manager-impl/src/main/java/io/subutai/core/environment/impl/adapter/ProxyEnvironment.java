package io.subutai.core.environment.impl.adapter;


import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


class ProxyEnvironment extends EnvironmentImpl
{
    // NOTE: Using environmentManager from EnvironmentImpl gives side effects. For example, empty container list.
    private EnvironmentManagerImpl environmentManager;

    ProxyEnvironment( String name, String subnetCidr, String sshKey, Long userId, String peerId )
    {
        super( name, subnetCidr, sshKey, userId, peerId );
    }


    public void setId( String id )
    {
        environmentId = id;
    }


    @Override
    public void setEnvironmentManager( final EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
    }
}

package io.subutai.core.hubmanager.impl.environment.state;


import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.processor.EnvironmentUserHelper;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;


public class Context
{
    public final IdentityManager identityManager;

    public final EnvironmentUserHelper envUserHelper;

    public final ConfigManager configManager;

    public final PeerManager peerManager;

    public final HubRestClient restClient;


    public Context( IdentityManager identityManager, EnvironmentUserHelper envUserHelper, ConfigManager configManager, PeerManager peerManager )
    {
        this.identityManager = identityManager;
        this.envUserHelper = envUserHelper;
        this.configManager = configManager;
        this.peerManager = peerManager;

        restClient = new HubRestClient( configManager );
    }
}

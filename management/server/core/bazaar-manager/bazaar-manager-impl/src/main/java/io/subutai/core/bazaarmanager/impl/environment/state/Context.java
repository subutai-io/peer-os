package io.subutai.core.bazaarmanager.impl.environment.state;


import io.subutai.common.peer.LocalPeer;
import io.subutai.core.desktop.api.DesktopManager;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.impl.util.EnvironmentUserHelper;
import io.subutai.core.identity.api.IdentityManager;


public class Context
{
    public final BazaarManager bazaarManager;

    public final IdentityManager identityManager;

    public final EnvironmentManager envManager;

    public final EnvironmentUserHelper envUserHelper;

    public final LocalPeer localPeer;

    public final RestClient restClient;

    public final DesktopManager desktopManager;


    public Context( BazaarManager bazaarManager, IdentityManager identityManager, EnvironmentManager envManager,
                    EnvironmentUserHelper envUserHelper, LocalPeer localPeer, RestClient restClient,
                    DesktopManager desktopManager )
    {
        this.bazaarManager = bazaarManager;

        this.identityManager = identityManager;

        this.envManager = envManager;

        this.envUserHelper = envUserHelper;

        this.localPeer = localPeer;

        this.restClient = restClient;

        this.desktopManager = desktopManager;
    }
}

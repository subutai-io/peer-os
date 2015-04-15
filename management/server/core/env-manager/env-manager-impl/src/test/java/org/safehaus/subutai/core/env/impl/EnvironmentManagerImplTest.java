package org.safehaus.subutai.core.env.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.environment.Blueprint;
import org.safehaus.subutai.core.env.impl.dao.BlueprintDataService;
import org.safehaus.subutai.core.env.impl.dao.EnvironmentContainerDataService;
import org.safehaus.subutai.core.env.impl.dao.EnvironmentDataService;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.tracker.api.Tracker;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerImplTest
{
    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    PeerManager peerManager;
    @Mock
    NetworkManager networkManager;
    @Mock
    DaoManager daoManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    Tracker tracker;
    @Mock
    EnvironmentDataService environmentDataService;
    @Mock
    EnvironmentContainerDataService environmentContainerDataService;
    @Mock
    BlueprintDataService blueprintDataService;
    @Mock
    Blueprint blueprint;

    EnvironmentManagerImpl environmentManager;


    @Before
    public void setUp() throws Exception
    {
        environmentManager = new EnvironmentManagerImpl( templateRegistry, peerManager, networkManager, daoManager,
                TestUtil.DEFAULT_DOMAIN, identityManager, tracker );
        environmentManager.environmentContainerDataService = environmentContainerDataService;
        environmentManager.blueprintDataService = blueprintDataService;
        environmentManager.environmentDataService = environmentDataService;
    }


    @Test
    public void testSaveBlueprint() throws Exception
    {
        environmentManager.saveBlueprint( blueprint );

        verify( blueprintDataService ).persist( blueprint );
    }
}

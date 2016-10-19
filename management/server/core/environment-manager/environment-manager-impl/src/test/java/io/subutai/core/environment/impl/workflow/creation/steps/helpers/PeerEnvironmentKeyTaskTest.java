package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.core.environment.impl.TestHelper;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class PeerEnvironmentKeyTaskTest
{

    PeerEnvironmentKeyTask task;

    @Mock
    PGPSecretKeyRing secretKeyRing;
    @Mock
    PGPPublicKeyRing pgpPublicKeyRing;
    LocalPeer LOCAL_PEER = TestHelper.LOCAL_PEER();


    @Before
    public void setUp() throws Exception
    {
        task = spy( new PeerEnvironmentKeyTask( LOCAL_PEER, secretKeyRing, pgpPublicKeyRing, TestHelper.ENVIRONMENT(),
                TestHelper.PEER(), TestHelper.KEY_MANAGER() ) );


        doReturn( pgpPublicKeyRing ).when( task ).getPublicKey( any( PublicKeyContainer.class ) );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( LOCAL_PEER ).addPeerEnvironmentPubKey( anyString(), any( PGPPublicKeyRing.class ) );
    }
}

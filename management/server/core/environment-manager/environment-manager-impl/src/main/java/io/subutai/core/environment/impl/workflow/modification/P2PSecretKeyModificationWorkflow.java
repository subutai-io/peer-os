package io.subutai.core.environment.impl.workflow.modification;


import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.modification.steps.P2PSecretKeyResetStep;


public class P2PSecretKeyModificationWorkflow
        extends CancellableWorkflow<P2PSecretKeyModificationWorkflow.P2PSecretKeyModificationPhase>
{
    public static final String P2P_CAUSE = "P2P key reset failed";
    private LocalEnvironment environment;
    private final String p2pSecretKey;
    private final long p2pSecretKeyTtlSeconds;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    public enum P2PSecretKeyModificationPhase
    {
        INIT, REPLACE_KEY, FINALIZE
    }


    public P2PSecretKeyModificationWorkflow( final LocalEnvironment environment, final String p2pSecretKey,
                                             final long p2pSecretKeyTtlSeconds, final TrackerOperation operationTracker,
                                             final EnvironmentManagerImpl environmentManager )
    {
        super( P2PSecretKeyModificationPhase.INIT );

        this.environment = environment;
        this.p2pSecretKey = p2pSecretKey;
        this.p2pSecretKeyTtlSeconds = p2pSecretKeyTtlSeconds;
        this.operationTracker = operationTracker;
        this.environmentManager = environmentManager;
    }


    //********************* WORKFLOW STEPS ************


    public P2PSecretKeyModificationPhase INIT()
    {
        operationTracker.addLog( "Initializing P2P secret key modification" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return P2PSecretKeyModificationPhase.REPLACE_KEY;
    }


    public P2PSecretKeyModificationPhase REPLACE_KEY()
    {

        operationTracker.addLog( "Modifying P2P secret key on peers" );

        try
        {
            new P2PSecretKeyResetStep( environment,
                    new P2PCredentials( environment.getId(), environment.getP2PHash(), p2pSecretKey,
                            p2pSecretKeyTtlSeconds ), operationTracker ).execute();

            saveEnvironment();

            return P2PSecretKeyModificationPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public void FINALIZE()
    {
        environment.setStatus( EnvironmentStatus.HEALTHY );

        saveEnvironment();

        operationTracker.addLogDone( "P2P secret key is modified" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );

        environment.setStatusDescription( P2P_CAUSE );

        saveEnvironment();

        operationTracker.addLogFailed( message );

        super.fail( message, e );
    }


    @Override
    public void onCancellation()
    {
        environment.setStatus( EnvironmentStatus.CANCELLED );

        saveEnvironment();

        operationTracker.addLogFailed( "P2P secret key modification was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}

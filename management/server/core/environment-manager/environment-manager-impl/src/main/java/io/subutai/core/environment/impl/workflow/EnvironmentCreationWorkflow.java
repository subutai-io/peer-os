package io.subutai.core.environment.impl.workflow;


import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentCreationWorkflow extends Workflow<EnvironmentCreationWorkflow.EnvironmentCreationPhase>
{
    private final PeerManager peerManager;
    private final Environment environment;
    private final Topology topology;
    private final String subnetCidr;
    private final String sshKey;
    private final TrackerOperation operationTracker;

    private Throwable error;


    public Throwable getError()
    {
        return error;
    }


    //environment creation phases
    public static enum EnvironmentCreationPhase
    {
        INIT,
        GENERATE_KEYS,
        SETUP_N2N,
        SETUP_VNI,
        CLONE_CONTAINERS,
        FINALIZE

    }


    public EnvironmentCreationWorkflow( PeerManager peerManager, Environment environment, Topology topology,
                                        String subnetCidr, String sshKey, TrackerOperation operationTracker )
    {
        super( EnvironmentCreationPhase.INIT );

        this.peerManager = peerManager;
        this.environment = environment;
        this.topology = topology;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.subnetCidr = subnetCidr;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentCreationPhase INIT()
    {
        System.out.println( "INIT" );

        return EnvironmentCreationPhase.GENERATE_KEYS;
    }


    public EnvironmentCreationPhase GENERATE_KEYS() throws InterruptedException
    {
        System.out.println( "GENERATE_KEYS" );

        Thread.sleep( 3000 );

        return EnvironmentCreationPhase.SETUP_N2N;
    }


    public EnvironmentCreationPhase SETUP_N2N()
    {
        System.out.println( "SETUP_N2N" );

        return EnvironmentCreationPhase.SETUP_VNI;
    }


    public EnvironmentCreationPhase SETUP_VNI()
    {
        System.out.println( "SETUP_VNI" );

        return EnvironmentCreationPhase.CLONE_CONTAINERS;
    }


    public EnvironmentCreationPhase CLONE_CONTAINERS()
    {
        System.out.println( "CLONE_CONTAINERS" );

        return EnvironmentCreationPhase.FINALIZE;
    }


    public void FINALIZE()
    {
        System.out.println( "FINALIZE" );

        //this is a must have call
        stop();
    }
}

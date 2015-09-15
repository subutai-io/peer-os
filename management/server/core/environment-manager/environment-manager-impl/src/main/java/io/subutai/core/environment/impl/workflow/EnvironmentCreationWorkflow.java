package io.subutai.core.environment.impl.workflow;


import org.apache.servicemix.beanflow.Workflow;


public class EnvironmentCreationWorkflow extends Workflow<EnvironmentCreationWorkflow.EnvironmentCreationPhase>
{
    private String subnetCidr;


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


    public EnvironmentCreationWorkflow( String subnetCidr )
    {
        super( EnvironmentCreationPhase.INIT );

        this.subnetCidr = subnetCidr;
    }


    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentCreationPhase INIT()
    {
        System.out.println( "INIT" );
        System.out.println(getSubnetCidr());

        return EnvironmentCreationPhase.GENERATE_KEYS;
    }


    public EnvironmentCreationPhase GENERATE_KEYS()
    {
        System.out.println( "GENERATE_KEYS" );

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

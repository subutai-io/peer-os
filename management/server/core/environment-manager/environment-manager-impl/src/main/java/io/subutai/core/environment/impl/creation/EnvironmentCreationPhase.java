package io.subutai.core.environment.impl.creation;


import org.apache.servicemix.beanflow.Workflow;
import org.apache.servicemix.beanflow.WorkflowStep;


/**
 * Phases of environment creation process
 */
public enum EnvironmentCreationPhase implements WorkflowStep<EnvironmentCreationPhase>
{
    INIT
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "INIT" );

                    EnvironmentCreationWorkflow environmentCreationWorkflow = ( EnvironmentCreationWorkflow ) workflow;

                    System.out.println( environmentCreationWorkflow.getSubnetCidr() );

                    return EXCHANGE_KEYS;
                }
            },
    EXCHANGE_KEYS
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "EXCHANGE_KEYS" );

                    return SETUP_N2N;
                }
            },
    SETUP_N2N
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "SETUP_N2N" );

                    return SETUP_VNI;
                }
            },
    SETUP_VNI
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "SETUP_VNI" );

                    return CLONE_CONTAINERS;
                }
            },
    CLONE_CONTAINERS
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "CLONE_CONTAINERS" );

                    return FINALIZE;
                }
            },
    FINALIZE
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "FINALIZE" );
                    EnvironmentCreationWorkflow environmentCreationWorkflow = ( EnvironmentCreationWorkflow ) workflow;

                    environmentCreationWorkflow.setCompleted( true );

                    workflow.stop();
                    return null;
                }
            }
}

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


                    //TODO initialize environment here

                    return GENERATE_KEYS;
                }
            },
    GENERATE_KEYS
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "GENERATE_KEYS" );

                    //TODO generate & exchange PEKs here

                    return SETUP_N2N;
                }
            },
    SETUP_N2N
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "SETUP_N2N" );


                    //TODO setup N2N here

                    return SETUP_VNI;
                }
            },
    SETUP_VNI
            {
                public EnvironmentCreationPhase execute( Workflow<EnvironmentCreationPhase> workflow )
                {
                    System.out.println( "SETUP_VNI" );

                    //TODO clone containers here

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

                    //TODO finalize environment creation here, e.g. save env/container metadata to database

                    EnvironmentCreationWorkflow environmentCreationWorkflow = ( EnvironmentCreationWorkflow ) workflow;

                    environmentCreationWorkflow.setCompleted( true );


                    workflow.stop();
                    return null;
                }
            }
}

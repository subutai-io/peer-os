package io.subutai.core.environment.impl.workflow.modification;


import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.modification.steps.ExportTemplateStep;
import io.subutai.core.environment.impl.workflow.modification.steps.PromoteTemplateStep;


public class TemplateCreationWorkflow
        extends CancellableWorkflow<TemplateCreationWorkflow.TemplateCreationWorkflowPhase>
{
    private final TrackerOperation operationTracker;
    private final String containerId;
    private final String templateName;
    private final boolean isPrivateTemplate;
    private final String token;

    private LocalEnvironment environment;


    public enum TemplateCreationWorkflowPhase
    {
        INIT, PROMOTE_TEMPLATE, EXPORT_TEMPLATE, FINALIZE
    }


    public TemplateCreationWorkflow( final LocalEnvironment environment, final String containerId,
                                     final String templateName, final boolean isPrivateTemplate, final String token,
                                     final TrackerOperation operationTracker )
    {
        super( TemplateCreationWorkflowPhase.INIT );

        this.environment = environment;
        this.operationTracker = operationTracker;
        this.containerId = containerId;
        this.templateName = templateName;
        this.isPrivateTemplate = isPrivateTemplate;
        this.token = token;
    }


    //********************* WORKFLOW STEPS ************


    public TemplateCreationWorkflowPhase INIT()
    {
        operationTracker.addLog( "Initializing template creation" );

        return TemplateCreationWorkflowPhase.PROMOTE_TEMPLATE;
    }


    public TemplateCreationWorkflowPhase PROMOTE_TEMPLATE()
    {

        operationTracker.addLog( "Promoting container to template" );

        try
        {
            new PromoteTemplateStep( environment, containerId, templateName ).execute();

            return TemplateCreationWorkflowPhase.EXPORT_TEMPLATE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public TemplateCreationWorkflowPhase EXPORT_TEMPLATE()
    {

        operationTracker.addLog( "Exporting template" );

        try
        {
            new ExportTemplateStep( environment, containerId, templateName, isPrivateTemplate, token ).execute();

            return TemplateCreationWorkflowPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public void FINALIZE()
    {

        operationTracker.addLogDone( "Template is created" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        operationTracker.addLogFailed( message );

        super.fail( message, e );
    }


    @Override
    public void onCancellation()
    {
        //no-op
    }
}

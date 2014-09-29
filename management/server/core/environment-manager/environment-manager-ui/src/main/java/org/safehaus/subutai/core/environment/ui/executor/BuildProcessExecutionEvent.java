package org.safehaus.subutai.core.environment.ui.executor;


import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by bahadyr on 9/23/14.
 */
public class BuildProcessExecutionEvent
{

    private BuildProcessExecutionEventType eventType;
    private EnvironmentBuildProcess environmentBuildProcess;


    public BuildProcessExecutionEvent( EnvironmentBuildProcess environmentBuildProcess,
                                       BuildProcessExecutionEventType type )
    {
        this.eventType = type;
        this.environmentBuildProcess = environmentBuildProcess;
    }


    public EnvironmentBuildProcess getEnvironmentBuildProcess()
    {
        return environmentBuildProcess;
    }


    public void setEnvironmentBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        this.environmentBuildProcess = environmentBuildProcess;
    }


    public BuildProcessExecutionEventType getEventType()
    {
        return eventType;
    }


    public void setEventType( final BuildProcessExecutionEventType eventType )
    {
        this.eventType = eventType;
    }
}

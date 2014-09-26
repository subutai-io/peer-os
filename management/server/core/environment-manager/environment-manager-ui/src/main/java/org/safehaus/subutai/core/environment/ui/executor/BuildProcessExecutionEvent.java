package org.safehaus.subutai.core.environment.ui.executor;


/**
 * Created by bahadyr on 9/23/14.
 */
public class BuildProcessExecutionEvent
{

    private String name;
    private String description;
    private BuildProcessExecutionEventType eventType;


    public BuildProcessExecutionEvent( String name, String description, BuildProcessExecutionEventType type )
    {
        this.name = name;
        this.description = description;
        this.eventType = type;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
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

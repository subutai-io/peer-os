package org.safehaus.subutai.core.environment.api.helper;


public enum ProcessStatusEnum
{
    NEW_PROCESS( "New" ), IN_PROGRESS( "In progress" ), FAILED( "Failed" ), TERMINATED( "Terminated" ),
    SUCCESSFUL( "OK" );
    String description;


    ProcessStatusEnum( final String description )
    {
        this.description = description;
    }


    public String getDescription()
    {
        return description;
    }
}

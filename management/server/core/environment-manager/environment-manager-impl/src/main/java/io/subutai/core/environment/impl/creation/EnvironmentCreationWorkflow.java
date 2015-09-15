package io.subutai.core.environment.impl.creation;


import org.apache.servicemix.beanflow.Workflow;


public class EnvironmentCreationWorkflow<T> extends Workflow<T>
{
    private String subnetCidr;

    private boolean completed;


    public EnvironmentCreationWorkflow( final Class<T> enumType, String subnetCidr )
    {
        super( enumType );

        this.subnetCidr = subnetCidr;
    }


    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    public boolean isCompleted()
    {
        return completed;
    }


    public void setCompleted( final boolean completed )
    {
        this.completed = completed;
    }
}

package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;
import java.util.Observable;


/**
 * Created by bahadyr on 11/5/14.
 */
public class EnvironmentBuilderThread extends Observable implements Runnable
{
    EnvironmentBuilderImpl environmentBuilder;
    List<ContainerDistributionMessage> messages;


    public EnvironmentBuilderThread( final EnvironmentBuilderImpl environmentBuilder,
                                     final List<ContainerDistributionMessage> messages )
    {
        this.environmentBuilder = environmentBuilder;
        this.messages = messages;
    }


    @Override
    public void run()
    {


        environmentBuilder.update( this, null );
    }



}

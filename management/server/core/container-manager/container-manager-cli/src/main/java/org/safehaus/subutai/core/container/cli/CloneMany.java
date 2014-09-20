package org.safehaus.subutai.core.container.cli;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.ContainerEvent;
import org.safehaus.subutai.core.container.api.ContainerEventListener;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.strategy.api.Criteria;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "container", name = "clone-many")
public class CloneMany extends OsgiCommandSupport implements ContainerEventListener
{

    ContainerManager containerManager;

    @Argument(index = 0, required = true)
    private String template;
    @Argument(index = 1, required = true)
    private int nodesCount;
    @Argument(index = 2, required = true)
    private String strategyId;
    @Argument(index = 3, required = false)
    private String criteriaList;


    public void setContainerManager( ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        containerManager.addListener( this );
        UUID envId = UUID.randomUUID();
        List<Criteria> criteria = getCriteria();
        Set<Agent> set = containerManager.clone( envId, template, nodesCount, strategyId, criteria );
        if ( set.isEmpty() )
        {
            System.out.println( "Result set is empty" );
        }
        else
        {
            System.out.println( "Returned clones: " + set.size() );
        }
        containerManager.removeListener( this );
        return null;
    }


    private List<Criteria> getCriteria()
    {
        List<Criteria> criteria = new ArrayList<>();
        if ( criteriaList != null )
        {
            String[] arr = criteriaList.split( "[\\s;,]" );

            for ( String c : arr )
            {
                criteria.add( new Criteria( c, c, true ) );
            }
        }
        return criteria;
    }


    @Override
    public void onContainerEvent( final ContainerEvent containerEvent )
    {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis( containerEvent.getTimestamp() );
        System.out.println(
                String.format( "%1$s: %2$s %3$s -< %4$s %5$tm %5$te,%5$tY %5$tT", containerEvent.getEventType(),
                        containerEvent.getEnvId(), containerEvent.getParentHostname(), containerEvent.getHostname(),
                        cal ) );
    }
}

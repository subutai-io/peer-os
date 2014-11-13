package org.safehaus.subutai.core.container.cli;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.container.api.ContainerEvent;
import org.safehaus.subutai.core.container.api.ContainerEventListener;
import org.safehaus.subutai.core.container.api.ContainerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
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
        UUID envId = UUIDUtil.generateTimeBasedUUID();
        ;
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
                criteria.add( new Criteria( c, true ) );
            }
        }
        return criteria;
    }


    @Override
    public void onContainerEvent( final ContainerEvent containerEvent )
    {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis( containerEvent.getTimestamp() );
    }
}

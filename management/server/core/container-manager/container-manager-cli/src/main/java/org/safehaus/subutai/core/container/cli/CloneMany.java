package org.safehaus.subutai.core.container.cli;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.strategy.api.Criteria;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "container", name = "clone-many")
public class CloneMany extends OsgiCommandSupport {

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
}

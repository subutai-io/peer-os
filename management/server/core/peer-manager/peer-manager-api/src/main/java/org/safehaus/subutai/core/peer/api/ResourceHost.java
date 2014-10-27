package org.safehaus.subutai.core.peer.api;


import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * Resource host implementation.
 */
public class ResourceHost extends SubutaiHost
{
    private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "State:(\\s*)(.*)" );
    private static final Pattern LOAD_AVERAGE_PATTERN = Pattern.compile( "load average: (.*)" );
    private static final long WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS = 10000;
    Set<ContainerHost> containersHosts = new HashSet();


    public ResourceHost( final Agent agent )
    {
        super( agent );
    }


    public void addContainerHost( ContainerHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Container host could not be null." );
        }
        containersHosts.add( host );
    }


    //    public HostImpl getChildHost( final String hostname )
    //    {
    //        HostImpl result = null;
    //        Iterator<ContainerHostImpl> iterator = containersHosts.iterator();
    //
    //        while ( result == null && iterator.hasNext() )
    //        {
    //            HostImpl host = iterator.next();
    //            if ( hostname.equals( host.getAgent().getHostname() ) )
    //            {
    //                result = host;
    //            }
    //        }
    //        return result;
    //    }


    public boolean startContainerHost( final ContainerHost container ) throws CommandException
    {

        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d &", container.getHostname() ) )
                        .withTimeout( 180 );
        execute( requestBuilder );
        try
        {
            Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
        }
        catch ( InterruptedException ignore )
        {
        }

        return ContainerState.RUNNING.equals( getContainerHostState( container ) );
    }


    private ContainerState getContainerHostState( final ContainerHost container ) throws CommandException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-info -n %s", container.getHostname() ) )
                        .withTimeout( 30 );
        CommandResult result = execute( requestBuilder );

        String stdOut = result.getStdOut();

        Matcher m = LXC_STATE_PATTERN.matcher( stdOut );
        if ( m.find() )
        {
            return ContainerState.valueOf( m.group( 2 ) );
        }
        else
        {
            return ContainerState.UNKNOWN;
        }
    }


    public ServerMetric getMetric() throws CommandException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( "free -m | grep buffers/cache ; df /lxc-data | grep /lxc-data ; uptime ; nproc" )
                        .withTimeout( 30 );
        CommandResult result = execute( requestBuilder );

        ServerMetric serverMetric = null;
        if ( result.hasCompleted() )
        {
            String[] metrics = result.getStdOut().split( "\n" );
            serverMetric = gatherMetrics( metrics );
            serverMetric.setAverageMetrics( gatherAvgMetrics() );
        }
        return serverMetric;
    }


    public Set<ContainerHost> getContainerHosts()
    {
        return containersHosts;
    }


    /**
     * Gather metrics from elastic search for a one week period
     */
    private Map<MetricType, Double> gatherAvgMetrics()
    {
        //TODO: Implement me
        Map<MetricType, Double> averageMetrics = new EnumMap<>( MetricType.class );
        return averageMetrics;
    }


    /**
     * Gather metrics from linux commands outputs.
     */
    private ServerMetric gatherMetrics( String[] metrics )
    {
        int freeRamMb = 0;
        int freeHddMb = 0;
        int numOfProc = 0;
        double loadAvg = 0;
        double cpuLoadPercent = 100;
        // parsing only 4 metrics
        if ( metrics.length != 4 )
        {
            return null;
        }
        boolean parseOk = true;
        for ( int line = 0; parseOk && line < metrics.length; line++ )
        {
            String metric = metrics[line];
            switch ( line )
            {
                case 0:
                    //-/+ buffers/cache:       1829       5810
                    String[] ramMetric = metric.split( "\\s+" );
                    String freeRamMbStr = ramMetric[ramMetric.length - 1];
                    try
                    {
                        freeRamMb = Integer.parseInt( freeRamMbStr );
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
                case 1:
                    //lxc-data       143264768 608768 142656000   1% /lxc-data
                    String[] hddMetric = metric.split( "\\s+" );
                    if ( hddMetric.length == 6 )
                    {
                        String hddMetricKbStr = hddMetric[3];
                        try
                        {
                            freeHddMb = Integer.parseInt( hddMetricKbStr ) / 1024;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 2:
                    // 09:17:38 up 4 days, 23:06,  0 users,  load average: 2.18, 3.06, 2.12
                    Matcher m = LOAD_AVERAGE_PATTERN.matcher( metric );
                    if ( m.find() )
                    {
                        String[] loads = m.group( 1 ).split( "," );
                        try
                        {
                            loadAvg = ( Double.parseDouble( loads[0] ) + Double.parseDouble( loads[1] ) + Double
                                    .parseDouble( loads[2] ) ) / 3;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 3:
                    try
                    {
                        numOfProc = Integer.parseInt( metric );
                        if ( numOfProc > 0 )
                        {
                            cpuLoadPercent = ( loadAvg / numOfProc ) * 100;
                        }
                        else
                        {
                            break;
                        }
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
            }
        }
        if ( parseOk )
        {
            ServerMetric serverMetric =
                    new ServerMetric( freeHddMb, freeRamMb, ( int ) cpuLoadPercent, numOfProc, null );
            return serverMetric;
        }
        else
        {
            return null;
        }
    }


    /**
     * Return first stdOut of the first agent
     *
     * @param command command object
     *
     * @return stdOut of the first agent
     */
    private String getStdOut( Command command )
    {
        return command.getResults().entrySet().iterator().next().getValue().getStdOut();
    }


    public boolean stopContainerHost( final ContainerHost container ) throws CommandException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s &", container.getHostname() ) )
                        .withTimeout( 180 );
        execute( requestBuilder );

        try
        {
            Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
        }
        catch ( InterruptedException ignore )
        {
        }

        return ContainerState.STOPPED.equals( getContainerHostState( container ) );
    }


    public ContainerHost getContainerHostByName( final String hostname )
    {
        ContainerHost result = null;
        Iterator iterator = containersHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ContainerHost host = ( ContainerHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
    }


    public Set<ContainerHost> getContainersHosts()
    {
        return containersHosts;
    }


    public void setContainersHosts( final Set<ContainerHost> containersHosts )
    {
        this.containersHosts = containersHosts;
    }


    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainersHosts() )
        {
            if ( containerHost.getEnvironmentId().equals( environmentId ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    @Override
    public boolean isConnected( final Host host )
    {
        try
        {
            return ContainerState.RUNNING.equals( getContainerHostState( ( ContainerHost ) host ) );
        }
        catch ( CommandException e )
        {
            return false;
        }
    }
}

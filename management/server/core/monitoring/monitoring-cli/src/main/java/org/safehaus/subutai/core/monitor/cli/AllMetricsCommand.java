package org.safehaus.subutai.core.monitor.cli;


import java.util.Date;
import java.util.Map;

import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.Monitor;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "monitor", name = "all-metrics")
public class AllMetricsCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "hostname", required = true, multiValued = false)
    protected String hostname = null;

    private Monitor monitor;


    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }


    protected Object doExecute()
    {

        Date endDate = new Date();
        Date startDate = DateUtils.addDays( endDate, -1 );

        Map<Metric, Map<Date, Double>> data = monitor.getDataForAllMetrics( hostname, startDate, endDate );

        System.out.println( "Data: " + data );

        return null;
    }
}

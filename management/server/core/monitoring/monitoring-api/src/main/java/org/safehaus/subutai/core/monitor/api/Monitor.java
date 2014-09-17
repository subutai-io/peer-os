package org.safehaus.subutai.core.monitor.api;


import java.util.Date;
import java.util.Map;


public interface Monitor {

    public Map<Date, Double> getData( String host, Metric metric, Date startDate, Date endDate );

    public Map<Metric, Map<Date, Double>> getDataForAllMetrics( String host, Date startDate, Date endDate );
}

package org.safehaus.kiskis.mgmt.api.monitor;

import java.util.Date;
import java.util.Map;

public interface Monitor {

    public Map<Date, Double> getData(String host, Metric metric, Date startDate, Date endDate);

}

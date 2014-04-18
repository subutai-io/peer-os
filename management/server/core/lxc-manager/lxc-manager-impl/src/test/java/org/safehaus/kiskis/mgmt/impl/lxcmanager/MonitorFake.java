/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.safehaus.kiskis.mgmt.api.monitor.Metric;
import org.safehaus.kiskis.mgmt.api.monitor.Monitor;

/**
 *
 * @author dilshat
 */
public class MonitorFake implements Monitor {

    public Map<Date, Double> getData(String host, Metric metric, Date startDate, Date endDate) {
        return Collections.EMPTY_MAP;
    }

}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.safehaus.Core.Threads;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.Label;
import org.safehaus.Core.ElasticSearchAccessObject;
import org.safehaus.Core.LogResponse;
import org.safehaus.Core.Timestamp;
import org.safehaus.UI.Log;
import org.safehaus.UI.Monitor;
import org.safehaus.UI.MonitorTab;
import org.safehaus.UI.Statistic;

import java.util.ArrayList;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class LogUpdateListener implements Refresher.RefreshListener {
    //Represents the lastIndex of the previous query result
    static int lastIndex;
    private static Timestamp beginTime;
    private Log log;
    /**
     *
     * @param lastIndex represents the lastIndex get from the query to Elasticsearch
     * @param lastHour  represents the time that we want to start from
     */
    public LogUpdateListener(Log log, int lastIndex, Timestamp lastHour)
    {
        this.lastIndex = lastIndex;
        beginTime = lastHour;
        this.log = log;

    }
    public void refresh(Refresher refresher) {
        System.out.println("Log is being refreshed! LastIndex: " + lastIndex +", beginTime: " + beginTime);
        MonitorTab monitorTab = Monitor.getMain().getMonitorTab();
        ArrayList<LogResponse> logResponses = monitorTab.getElasticSearchAccessObject().getLogs(lastIndex, beginTime, Timestamp.getCurrentTimestamp());
        monitorTab.getLogTable().fillTable(logResponses,lastIndex);
        lastIndex += logResponses.size();
    }
}

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
package org.safehaus.UI;

import com.vaadin.addon.charts.Chart;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import org.elasticsearch.action.search.SearchResponse;
import org.safehaus.Core.LogResponse;
import org.safehaus.Core.Timestamp;

import java.util.ArrayList;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class HistoryMetricList extends VerticalLayout implements
        Property.ValueChangeListener {
    public ComboBox getSampleMetricList()
    {

        ComboBox l = new ComboBox("Please select history metric");
        l.addItem(1);
        l.addItem(2);
        l.addItem(3);
        l.addItem(4);
        l.setItemCaption(1, "1 hour");
        l.setItemCaption(2, "2 hours");
        l.setItemCaption(3, "12 hours");
        l.setItemCaption(4, "1 day");

        l.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        l.setImmediate(true);
        l.setNullSelectionAllowed(false);
        l.addListener(this);


        return l;

    }
    public void valueChange(Property.ValueChangeEvent event) {
        Timestamp currentTime = Timestamp.getCurrentTimestamp();
        Timestamp lastHour = Timestamp.getHoursEarlier(currentTime,1);
        MonitorTab monitorTab = Monitor.getMain().getMonitorTab();
        if(event.getProperty().toString().equalsIgnoreCase("1"))
        {
            lastHour = Timestamp.getHoursEarlier(currentTime,1);
        }
        else if(event.getProperty().toString().equalsIgnoreCase("2"))
        {
            lastHour = Timestamp.getHoursEarlier(currentTime,2);
        }
        else if(event.getProperty().toString().equalsIgnoreCase("3"))
        {
            lastHour = Timestamp.getHoursEarlier(currentTime,12);
        }
        else if(event.getProperty().toString().equalsIgnoreCase("4"))
        {
            lastHour = Timestamp.getHoursEarlier(currentTime,24);
        }
        else
        {
        }
        // Update Chart according to the history metric
        int chartIndex = monitorTab.getComponentIndex(monitorTab.getChart());
        monitorTab.removeComponent(monitorTab.getChart());
        SearchResponse response = monitorTab.getElasticSearchAccessObject().executeMemoryQuery(-1, lastHour, currentTime);
        Statistic memoryStatistic = new Statistic();
        monitorTab.setChart(memoryStatistic.getMemoryChart(response));
        monitorTab.addComponent(monitorTab.getChart(), chartIndex);
        monitorTab.addStatisticRefresher((int) response.getHits().getTotalHits(), monitorTab.getStatisticUpdateInterval());


        // Update Log according to the history metric
        int logIndex = monitorTab.getComponentIndex(monitorTab.getLogTable());
        monitorTab.removeComponent(monitorTab.getLogTable());
        ArrayList<LogResponse> logResponses = monitorTab.getElasticSearchAccessObject().getLogs(-1, lastHour, currentTime);
        Log logTable  = new Log();
        logTable.fillTable(logResponses, -1);
        monitorTab.setLogTable(logTable);
        monitorTab.addComponent(monitorTab.getLogTable(), logIndex);
        monitorTab.addLogsRefresher(logResponses.size(), monitorTab.getStatisticUpdateInterval());

    }
}
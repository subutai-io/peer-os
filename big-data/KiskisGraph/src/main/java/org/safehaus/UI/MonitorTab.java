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

import com.github.wolfie.refresher.Refresher;
import com.vaadin.addon.charts.Chart;
import com.vaadin.ui.*;
import org.elasticsearch.action.search.SearchResponse;
import org.safehaus.Core.ElasticSearchAccessObject;
import org.safehaus.Core.LogResponse;
import org.safehaus.Core.Threads.LogUpdateListener;
import org.safehaus.Core.Threads.MemoryChartListener;
import org.safehaus.Core.Timestamp;

import java.util.ArrayList;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class MonitorTab extends VerticalLayout {
    private int statisticUpdateInterval = 10000;
    private MetricList metricList;
    Timestamp lastHour;
    private static int hourCount = 1;
    private Refresher memoryChartRefresher;
    private Refresher logRefresher;
    private MemoryChartListener memoryChartListener;
    private LogUpdateListener logUpdateListener;
    private Chart chart;
    private HorizontalLayout chartPanel = new HorizontalLayout();
    private Statistic memoryStatistic;
    private Log logTable;
    private ElasticSearchAccessObject ESAO;

    public MonitorTab()
    {
        Timestamp currentTime = Timestamp.getCurrentTimestamp();
        lastHour = Timestamp.getHoursEarlier(currentTime,1);
        ESAO = new ElasticSearchAccessObject();
        SearchResponse statisticResponse = ESAO.executeMemoryQuery(-1, lastHour, currentTime);
        ArrayList<LogResponse> logResponses = ESAO.getLogs(-1, lastHour, currentTime);
        memoryStatistic = new Statistic();
        logTable = new Log();
        setChart(memoryStatistic.getMemoryChart(statisticResponse));
        logTable.fillTable(logResponses, -1);
        metricList = (new MetricList());
        final ComboBox comboBox = metricList.getSampleMetricList();

        HistoryMetricList historyMetricList = new HistoryMetricList();
        final ComboBox comboBox2 = historyMetricList.getSampleMetricList();
        final Button update = new Button("update Hour");

        addComponent(comboBox);
        addComponent(comboBox2);
        addComponent(update);
        addComponent(getChart());
//        addComponent(chartPanel);
        addComponent(logTable);

        update.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
//                chart.requestRepaint();
                hourCount++;
                System.out.println("Hour count: " + hourCount);
                int index = getComponentIndex(getChart());
                removeComponent(getChart());
                Timestamp currentTime = Timestamp.getCurrentTimestamp();
                lastHour = Timestamp.getHoursEarlier(currentTime,hourCount);
                SearchResponse response = ESAO.executeMemoryQuery(-1, lastHour, currentTime);
                memoryStatistic = new Statistic();
                setChart(memoryStatistic.getMemoryChart(response));
                addComponent(getChart(), index);
                addStatisticRefresher((int) response.getHits().getTotalHits(), getStatisticUpdateInterval());
            }
        });

        addLogsRefresher(logResponses.size(), statisticUpdateInterval);
        addStatisticRefresher((int) statisticResponse.getHits().getTotalHits(), statisticUpdateInterval);

    }


    public void addStatisticRefresher(int lastIndex, int interval) {
        if(memoryChartRefresher != null)
        {
            removeComponent(memoryChartRefresher);
        }
        memoryChartRefresher = new Refresher();
        memoryChartRefresher.setRefreshInterval(interval);
        memoryChartListener = new MemoryChartListener(memoryStatistic, lastIndex, lastHour);
        memoryChartRefresher.addListener(memoryChartListener);
        addComponent(memoryChartRefresher);
    }

    public void addLogsRefresher(int lastIndex, int interval) {
        if(logRefresher != null)
        {
            removeComponent(logRefresher);
        }
        logRefresher = new Refresher();
        logUpdateListener = new LogUpdateListener(logTable, lastIndex, lastHour);
        logRefresher.setRefreshInterval(interval);
        logRefresher.addListener(logUpdateListener);
        addComponent(logRefresher);
    }

    public ElasticSearchAccessObject getElasticSearchAccessObject() {
        return ESAO;
    }

    public void setElasticSearchAccessObject(ElasticSearchAccessObject ESAO) {
        this.ESAO = ESAO;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public int getStatisticUpdateInterval() {
        return statisticUpdateInterval;
    }

    public void setStatisticUpdateInterval(int statisticUpdateInterval) {
        this.statisticUpdateInterval = statisticUpdateInterval;
    }



    public Log getLogTable() {
        return logTable;
    }

    public void setLogTable(Log logTable) {
        this.logTable = logTable;
    }
}

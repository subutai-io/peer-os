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
package org.safehaus.ui;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.safehaus.core.ElasticSearchAccessObject;
import org.safehaus.core.LogResponse;
import org.safehaus.core.refreshListeners.ChartListener;
import org.safehaus.core.refreshListeners.LogListener;
import org.safehaus.core.Timestamp;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    Linker linker;
    private Refresher memoryChartRefresher;
    private Refresher logRefresher;
    private ChartListener memoryChartListener;
    private LogListener logUpdateListener;
    private HorizontalLayout chartPanel = new HorizontalLayout();
    private StatisticChart statisticChart;
    private Log logTable;
    private ElasticSearchAccessObject ESAO;
    private HistoryMetricList historyMetricList;
    private BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

    private Logger logger;
    public MonitorTab()
    {
        HorizontalLayout metricLayout = new HorizontalLayout();
        linker = new Linker();
        logger = Logger.getLogger("MonitorTabLogger");

        setMetricList((new MetricList()));
        setHistoryMetricList(new HistoryMetricList());


        Timestamp currentTime = Timestamp.getCurrentTimestamp();
        lastHour = Timestamp.getHoursEarlier(currentTime,1);
        ESAO = new ElasticSearchAccessObject();
        ArrayList<LogResponse> logResponses = new ArrayList<LogResponse>();
        statisticChart = new StatisticChart(null);
        logTable = new Log();
        setStatisticChart((getStatisticChart().getDefaultChart()));
        logTable.fillTable(logResponses, -1);



        final ComboBox comboBox = getMetricList().getMetricList();
        final ComboBox comboBox2 = getHistoryMetricList().getSampleMetricList();
//        addComponent(linker);
        metricLayout.addComponent(comboBox);
        metricLayout.addComponent(comboBox2);
        metricLayout.setComponentAlignment(comboBox2, Alignment.MIDDLE_RIGHT);

        addComponent(linker);
        addComponent(metricLayout);
        addComponent(statisticChart);
        addComponent(logTable);

        addLogsRefresher(logResponses.size(), statisticUpdateInterval,queryBuilder);
        addStatisticRefresher(0, statisticUpdateInterval, queryBuilder);

    }

    public void addStatisticRefresher(int lastIndex, int interval, BoolQueryBuilder queryBuilder) {
        if(memoryChartRefresher != null)
        {
            removeComponent(memoryChartRefresher);
        }
        else
        {
            logger.log(Level.INFO, "No Chart Refresher Found, It should be in initalization step...");
        }
        memoryChartRefresher = new Refresher();
        memoryChartRefresher.setRefreshInterval(interval);
        memoryChartListener = new ChartListener(queryBuilder, getStatisticChart(), lastIndex, getHistoryMetricList().getLastHour());
        memoryChartRefresher.addListener(memoryChartListener);
        this.addComponent(memoryChartRefresher);
        this.requestRepaint();
    }

    public void updateChart()
    {
        if(getMetricList().getTermQueryBuilderList().size() == 0)
        {
            Monitor.getMain().showNotification("Please select one metric!");
            return;
        }
        Timestamp currentTime = Timestamp.getCurrentTimestamp();
        int chartIndex = getComponentIndex(getStatisticChart());
        if(chartIndex != -1)
            removeComponent(getStatisticChart());

        SearchResponse response = ESAO.executeQuery(memoryChartListener.getAllMetricsQuery(), -1, getHistoryMetricList().getLastHour(), currentTime);
        statisticChart = new StatisticChart(response);
        addStatisticRefresher(response.getHits().getHits().length, getStatisticUpdateInterval(), memoryChartListener.getAllMetricsQuery());
        setStatisticChart(statisticChart.getDefaultChart());

        if(chartIndex != -1)
            addComponent(getStatisticChart(), chartIndex);
        else
            addComponent(getStatisticChart());
        requestRepaintAll();
    }

    public void updateLog() {
        Timestamp currentTime = Timestamp.getCurrentTimestamp();
        int logIndex = getComponentIndex(getLogTable());
        if(logIndex != -1)
            removeComponent(getLogTable());
        ArrayList<LogResponse> logResponses = ESAO.getLogs(logUpdateListener.getAllMetricsQuery(), -1, getHistoryMetricList().getLastHour(), currentTime);
        Log logTable  = new Log();
        logTable.fillTable(logResponses, -1);
        addLogsRefresher(logResponses.size(), getStatisticUpdateInterval(),logUpdateListener.getAllMetricsQuery());
        setLogTable(logTable);
        if(logIndex != -1)
            addComponent(getLogTable(), logIndex);
        else
            addComponent(getLogTable());
        requestRepaintAll();
    }

    public void addLogsRefresher(int lastIndex, int interval, BoolQueryBuilder queryBuilder) {
        if(logRefresher != null)
        {
            removeComponent(logRefresher);
        }
        logRefresher = new Refresher();
        logUpdateListener = new LogListener(queryBuilder, logTable, lastIndex, getHistoryMetricList().getLastHour());
        logRefresher.setRefreshInterval(interval);
        logRefresher.addListener(logUpdateListener);
        this.addComponent(logRefresher);
        this.requestRepaint();
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

    public ChartListener getMemoryChartListener() {
        return memoryChartListener;
    }

    private void setMemoryChartListener(ChartListener memoryChartListener) {
        this.memoryChartListener = memoryChartListener;
    }

    public MetricList getMetricList() {
        return metricList;
    }

    public void setMetricList(MetricList metricList) {
        this.metricList = metricList;
    }

    public HistoryMetricList getHistoryMetricList() {
        return historyMetricList;
    }

    public void setHistoryMetricList(HistoryMetricList historyMetricList) {
        this.historyMetricList = historyMetricList;
    }

    public StatisticChart getStatisticChart() {
        return statisticChart;
    }

    public void setStatisticChart(StatisticChart statisticChart) {
        this.statisticChart = statisticChart;
    }


}

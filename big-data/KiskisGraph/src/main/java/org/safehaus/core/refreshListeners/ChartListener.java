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
package org.safehaus.core.refreshListeners;

import com.github.wolfie.refresher.Refresher;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.safehaus.core.ElasticSearchAccessObject;
import org.safehaus.core.StatisticResponse;
import org.safehaus.core.Timestamp;
import org.safehaus.ui.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class ChartListener implements Refresher.RefreshListener {
    //Represents the lastIndex of the previous query result
    static int lastIndex;
    private Timestamp beginTime;
    private StatisticChart statisticChart;
    private Logger logger = Logger.getLogger("ChartUpdateLogger");
    private ElasticSearchAccessObject ESAO = new ElasticSearchAccessObject();
    private BoolQueryBuilder queryBuilder;
    private ReferenceComponent referenceComponent;

    /**
     *
     * @param lastIndex represents the lastIndex get from the query to Elasticsearch
     * @param lastHour  represents the time that we want to start from
     */
    public ChartListener(ReferenceComponent referenceComponent, BoolQueryBuilder queryBuilder, StatisticChart statisticChart, int lastIndex, Timestamp lastHour) {
        this.referenceComponent = referenceComponent;
        this.queryBuilder = queryBuilder;
        this.lastIndex = lastIndex;
        setBeginTime(lastHour);
        this.statisticChart = statisticChart;
    }

    public void refresh(Refresher refresher) {
        logger.log(Level.INFO, "Chart is being refreshed!");

        SearchResponse response = ESAO.executeQuery(queryBuilder, lastIndex, getBeginTime(), Timestamp.getCurrentTimestamp());
        StatisticResponse statisticResponse = new StatisticResponse(response, ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab().getMetricList().getMetricValue());
        lastIndex = (int)response.getHits().getTotalHits();

        statisticChart.addData(statisticResponse);
        if(statisticResponse.getResponseCount() == 0)
        {
            System.out.println("Added: No data!");

        }
        else if(statisticResponse.getResponseCount() < 10)
        {
            for(int i = 0; i< statisticResponse.getResponseCount(); i++)
            {
                System.out.println("Added: " + statisticResponse.getValues()[i]);
            }
        }
        else
            System.out.println("Added: More than 10 points!" );
//

    }
    public Timestamp getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
    }

    public BoolQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(BoolQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
    public void resetQueryBuilder()
    {
        queryBuilder = QueryBuilders.boolQuery();
    }
    public BoolQueryBuilder getAllMetricsQuery()
    {
        BoolQueryBuilder queryBuilder =  QueryBuilders.boolQuery();
        MonitorTab monitorTab = ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab();
        Host host = ((Monitor) referenceComponent.getApplication()).getMain().getHosts();
        MetricList metricList = monitorTab.getMetricList();
        if(metricList != null)
        {
            for(int i = 0; i < metricList.getTermQueryBuilderList().size(); i++)
            {
                queryBuilder = queryBuilder.must(metricList.getTermQueryBuilderList().get(i));
            }
        }

        if(host != null && host.getTermQueryBuilder() != null)
        {
            queryBuilder = queryBuilder.must(host.getTermQueryBuilder());
        }

        return queryBuilder;
    }

}

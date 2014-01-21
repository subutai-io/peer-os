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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.safehaus.core.ElasticSearchAccessObject;
import org.safehaus.core.LogResponse;
import org.safehaus.core.Timestamp;
import org.safehaus.ui.*;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.elasticsearch.index.query.QueryBuilders.queryString;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class LogListener implements Refresher.RefreshListener {
    //Represents the lastIndex of the previous query result
    static int lastIndex;
    private static Timestamp beginTime;
    private Log log;
    private Logger logger = Logger.getLogger("LogUpdateLogger");
    private ElasticSearchAccessObject ESAO = new ElasticSearchAccessObject();
    private BoolQueryBuilder queryBuilder;
    private ReferenceComponent referenceComponent;
    /**
     *
     * @param lastIndex represents the lastIndex get from the query to Elasticsearch
     * @param lastHour  represents the time that we want to start from
     */
    public LogListener(ReferenceComponent referenceComponent, BoolQueryBuilder queryBuilder, Log log, int lastIndex, Timestamp lastHour)
    {
        this.referenceComponent = referenceComponent;
        this.lastIndex = lastIndex;
        beginTime = lastHour;
        this.log = log;
        this.queryBuilder = queryBuilder;

    }
    public void refresh(Refresher refresher) {
        logger.log(Level.INFO, "Log is being refreshed! LastIndex: " + lastIndex +", beginTime: " + beginTime);
        ArrayList<LogResponse> logResponses = ESAO.getLogs(queryBuilder, lastIndex, beginTime, Timestamp.getCurrentTimestamp());
        log.fillTable(logResponses, lastIndex);
        log.requestRepaintAll();
        lastIndex += logResponses.size();

        if(logResponses.size() == 0)
        {
            System.out.println("Added: No data!");

        }
        else if(logResponses.size() < 10)
        {
            for(int i = 0; i< logResponses.size(); i++)
            {
                LogResponse logResponse = logResponses.get(i);
                System.out.println("Added: Timestamp: " + logResponse.getTimestamp() + "; host: " + logResponse.getHost() + "; path: " + logResponse.getPath());
            }
        }
        else
            System.out.println("Added: More than 10 points!" );

    }

    public BoolQueryBuilder getAllMetricsQuery() {

        BoolQueryBuilder queryBuilder =  QueryBuilders.boolQuery();
        Host host = ((Monitor) referenceComponent.getApplication()).getMain().getHosts();

        if(host != null && host.getLogHostTermQueryBuilder() != null)
        {
            for(int i = 0; i < host.getChartHostTermQueryBuilder().size(); i++)
            {
                queryBuilder = queryBuilder.must(host.getLogHostTermQueryBuilder().get(i));
            }
            queryBuilder = queryBuilder.must(queryString("path:*log"));
        }

        return queryBuilder;
    }
}

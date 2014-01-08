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

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Tree;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.safehaus.core.ElasticSearchAccessObject;

import java.util.ArrayList;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class Host {
    private BaseQueryBuilder termQueryBuilder = null;
    private String listofHosts = "List of Hosts:";

    public Tree getRealHostTree()
    {
        ElasticSearchAccessObject ESAO = new ElasticSearchAccessObject();
        ArrayList<String> hosts = ESAO.getHosts();

        Tree nodes = new Tree(listofHosts);
        nodes.addItem(listofHosts);
        for(int i=0; i<hosts.size(); i++){
            nodes.addItem(hosts.get(i));
            nodes.setParent(hosts.get(i), listofHosts);
            nodes.setChildrenAllowed(hosts.get(i), false);
        }

        nodes.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                MonitorTab monitorTab = Monitor.getMain().getMonitorTab();
                String hostName = event.getItemId().toString().toLowerCase();
                String hostNameforLog = event.getItemId().toString();
                if(event.getItemId().toString().equalsIgnoreCase(listofHosts))
                {
                    hostNameforLog = "";
                }
                showLogTableByHost(Monitor.getMain().getMonitorTab().getLogTable(), hostNameforLog);
                setTermQueryBuilder(termQuery("log_host", hostName));
                monitorTab.updateChart();
            }
        });
        return nodes;
    }
    public void showLogTableByHost(Log logTable, String hostName)
    {
        logTable.getNormalFilterTable().setFilterFieldValue("host", hostName);
        logTable.getPagedFilterTable().setFilterFieldValue("host", hostName);

    }
    public BaseQueryBuilder getTermQueryBuilder() {
        return termQueryBuilder;
    }

    public void setTermQueryBuilder(BaseQueryBuilder termQueryBuilder) {
        this.termQueryBuilder = termQueryBuilder;
    }
}

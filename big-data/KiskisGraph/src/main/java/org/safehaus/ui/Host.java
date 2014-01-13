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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
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
    private ArrayList<BaseQueryBuilder> termQueryBuilder =  new ArrayList<BaseQueryBuilder>();
    private BaseQueryBuilder hostTermQueryBuilder = termQuery("host", "");
    private String listofHosts = "List of Hosts:";
    private ReferenceComponent referenceComponent;

    public Host(ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
    }
    public Tree getRealHostTree()
    {
        ElasticSearchAccessObject ESAO = new ElasticSearchAccessObject();
        ArrayList<String> hosts = ESAO.getHosts();

        final Tree nodes = new Tree(listofHosts);
        for(int i=0; i<hosts.size(); i++){
            nodes.addItem(hosts.get(i));
            nodes.setParent(hosts.get(i), nodes);
            nodes.setChildrenAllowed(hosts.get(i), false);
        }

        nodes.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                MonitorTab monitorTab = ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab();
                String hostName = event.getItemId().toString().toLowerCase();
                String hostNameforLog = event.getItemId().toString();
                ArrayList<BaseQueryBuilder> baseQueryBuilders = new ArrayList<BaseQueryBuilder>();
                showLogTableByHost(((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab().getLogTable(), hostNameforLog);
                if(!hostName.contains("-"))
                    baseQueryBuilders.add(termQuery("log_host", hostName));
                else
                {
                    ArrayList<String> hostNameList = setHostNameQueryBuilder(hostName);
                    for(int i=0; i < hostNameList.size(); i++ )
                    {
                       baseQueryBuilders.add(termQuery("log_host", hostNameList.get(i)));
                    }
                }
                setTermQueryBuilder(baseQueryBuilders);
                setHostTermQueryBuilder(termQuery("host", hostName));
                monitorTab.updateChart();
                monitorTab.updateLog();
            }

            private ArrayList<String> setHostNameQueryBuilder(String hostName) {
                ArrayList<String> hostNameList = new ArrayList<String>();
                String[] split = hostName.split("-");
                for(int i = 0; i< split.length; i++)
                    hostNameList.add(i, split[i]);
                return hostNameList;
            }
        });
        return nodes;
    }
    public void showLogTableByHost(Log logTable, String hostName)
    {
        logTable.getNormalFilterTable().setFilterFieldValue("host", hostName);
        logTable.getPagedFilterTable().setFilterFieldValue("host", hostName);

    }
    public ArrayList<BaseQueryBuilder> getTermQueryBuilder() {
        return termQueryBuilder;
    }

    public void setTermQueryBuilder(ArrayList<BaseQueryBuilder> termQueryBuilder) {
        this.termQueryBuilder = termQueryBuilder;
    }

    public Tree getSampleHosts() {
        Tree nodes = new Tree(listofHosts);
        nodes.addItem("Node1");
        nodes.addItem("Node2");
        nodes.addItem("Node3");
        nodes.addItem("Node4");
        nodes.setParent("Node1", listofHosts);
        nodes.setParent("Node2", listofHosts);
        nodes.setParent("Node3", listofHosts);
        nodes.setParent("Node4", listofHosts);
        nodes.setChildrenAllowed("Node1", false);
        nodes.setChildrenAllowed("Node2", false);
        nodes.setChildrenAllowed("Node3", false);
        nodes.setChildrenAllowed("Node4", false);
        return nodes;
    }

    public BaseQueryBuilder getHostTermQueryBuilder() {
        return hostTermQueryBuilder;
    }

    public void setHostTermQueryBuilder(BaseQueryBuilder hostTermQueryBuilder) {
        this.hostTermQueryBuilder = hostTermQueryBuilder;
    }
}

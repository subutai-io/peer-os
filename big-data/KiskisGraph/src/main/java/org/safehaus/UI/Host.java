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

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Tree;
import org.safehaus.Core.ElasticSearchAccessObject;

import java.util.ArrayList;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class Host {
    public static Tree getSampleHostTree()
    {
        HierarchicalContainer container;
        container = new HierarchicalContainer();
        container.addItem("Physical1");
        container.addItem("Physical2");
        container.addItem("Node1");
        container.addItem("Node2");
        container.addItem("Node3");
        container.addItem("Node4");
        container.addItem("Node5");
        container.setParent("Node1","Physical1");
        container.setParent("Node2","Physical1");
        container.setParent("Node3","Physical1");
        container.setParent("Node4","Physical2");
        container.setParent("Node5","Physical2");


        Tree tree = new Tree("List of Hosts:", container);
        tree.setChildrenAllowed("Node1",false);
        tree.setChildrenAllowed("Node2",false);
        tree.setChildrenAllowed("Node3",false);
        tree.setChildrenAllowed("Node4",false);
        tree.setChildrenAllowed("Node5",false);


        return tree;
    }


    public static Tree getRealHostTree(){
        ElasticSearchAccessObject ESAO = new ElasticSearchAccessObject();
        ArrayList<String> hosts = ESAO.getHosts();

        Tree nodes = new Tree();
        nodes.addItem("List of Hosts:");
        for(int i=0; i<hosts.size(); i++){
            nodes.addItem(hosts.get(i));
            nodes.setParent(hosts.get(i), "List of Hosts:");
            nodes.setChildrenAllowed(hosts.get(i), false);
        }

        nodes.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                MonitorTab monitorTab = Monitor.getMain().getMonitorTab();
                monitorTab.getLogTable().getNormalFilterTable().setFilterFieldValue("host", event.getItemId());
                monitorTab.getLogTable().getPagedFilterTable().setFilterFieldValue("host", event.getItemId());
            }
        });
        return nodes;
    }


//    public Tree getSampleHostTree(HorizontalSplitPanel hsp, final FilterTable normal, final FilterTable paged){
//        VerticalLayout verticalLayout = new VerticalLayout();
//        verticalLayout.setMargin(true);
//        ElasticSearchJavaApi es = new ElasticSearchJavaApi();
//        final ArrayList<String> hosts = new ArrayList<String>();
//        es.getHosts(hosts);
//
//        Tree nodes = new Tree("Machines");
//        //nodes.addItem("Machines");
//        for(int i=0; i<hosts.size(); i++){
//            nodes.addItem(hosts.get(i));
//            nodes.setParent(hosts.get(i), "Machines");
//            nodes.setChildrenAllowed(hosts.get(i), false);
//            verticalLayout.addComponent(nodes);
//        }
//        hsp.addComponent(verticalLayout);
//
//        nodes.addListener(new ItemClickEvent.ItemClickListener() {
//            public void itemClick(ItemClickEvent event) {
//                normal.setFilterFieldValue("host", event.getItemId());
//                paged.setFilterFieldValue("host", event.getItemId());
//            }
//        });
//        return nodes;
//    }
}

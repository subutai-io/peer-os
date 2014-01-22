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

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import org.elasticsearch.index.query.TermQueryBuilder;

import java.util.ArrayList;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */

public class MetricList extends VerticalLayout implements Property.ValueChangeListener {
    private ArrayList<TermQueryBuilder> termQueryBuilderList = new ArrayList<TermQueryBuilder>();
    // Set this value to the columnName that includes the data to be displayed for that metric type
    private String metricValue = null;
    private String metricType = "";
    ComboBox l = new ComboBox("Please select your metric");
    private ReferenceComponent referenceComponent;

    private enum MetricNameQueryPair {
        metric1("Bytes In", termQuery("name", "bytes_in"),"val",1),
        metric2("Bytes Out", termQuery("name", "bytes_out"),"val",2),
        metric3("CPU Aidle", termQuery("name", "cpu_aidle"),"val",3),
        metric32("CPU Idle", termQuery("name", "cpu_idle"),"val",4),
        metric4("CPU Nice", termQuery("name", "cpu_nice"),"val",5),
        metric5("CPU System", termQuery("name", "cpu_system"),"val",6),
        metric6("CPU User", termQuery("name", "cpu_user"),"val",7),
        metric7("CPU Wio", termQuery("name", "cpu_wio"),"val",8),
        metric8("Load One", termQuery("name", "load_one"),"val",9),
        metric9("Load Five", termQuery("name", "load_five"),"val",10),
        metric10("Load Fifteen", termQuery("name", "load_fifteen"),"val",11),
        metric11("Memory Buffers", termQuery("name", "mem_buffers"),"val",12),
        metric12("Memory Cached", termQuery("name", "mem_cached"),"val",13),
        metric13("Memory Free", termQuery("name", "mem_free"),"val",14),
        metric14("Memory Shared", termQuery("name", "mem_shared"),"val",15),
        metric15("Memory Total", termQuery("name", "mem_total"),"val",16),
        metric16("Part Max Used", termQuery("name", "part_max_used"),"val",17),
        metric17("Packets In", termQuery("name", "pkts_in"),"val",18),
        metric18("Packets Out", termQuery("name", "pkts_out"),"val",19),
        metric19("Swap Free", termQuery("name", "swap_free"),"val",20),
        metric20("Swap Total", termQuery("name", "swap_total"),"val",21),
        metric21("JVM HeapMemoryUsage_init", termQuery("name", "sun_management_MemoryImpl.HeapMemoryUsage_init".toLowerCase()),"val",22),
        metric22("JVM HeapMemoryUsage_max", termQuery("name", "sun_management_MemoryImpl.HeapMemoryUsage_max".toLowerCase()),"val",23),
        metric23("JVM HeapMemoryUsage_used", termQuery("name", "sun_management_MemoryImpl.HeapMemoryUsage_used".toLowerCase()),"val",24),
        metric24("JVM NonHeapMemoryUsage_committed", termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_committed".toLowerCase()),"val",25),
        metric25("JVM NonHeapMemoryUsage_init", termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_init".toLowerCase()),"val",26),
        metric26("JVM NonHeapMemoryUsage_max", termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_max".toLowerCase()),"val",27),
        metric27("JVM NonHeapMemoryUsage_used", termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_used".toLowerCase()),"val",28),
        metric28("JVM DaemonThreadCount", termQuery("name", "sun_management_ThreadImpl.DaemonThreadCount".toLowerCase()),"val",29),
        metric29("JVM PeakThreadCount", termQuery("name", "sun_management_ThreadImpl.PeakThreadCount".toLowerCase()),"val",30),
        metric30("JVM ThreadCount", termQuery("name", "sun_management_ThreadImpl.ThreadCount".toLowerCase()),"val",31),
        metric31("JVM TotalStartedThreadCount", termQuery("name", "sun_management_ThreadImpl.TotalStartedThreadCount".toLowerCase()),"val",32),
        ;
        String name;
        private TermQueryBuilder query;
        String metricValue;
        int id;
        MetricNameQueryPair(String name, TermQueryBuilder query, String metricValue, int id)
        {
            this.name = name;
            this.query = query;
            this.metricValue = metricValue;
            this.id = id;
        }
        MetricNameQueryPair(String name, TermQueryBuilder query, String metricValue)
        {
            this.name = name;
            this.query = query;
            this.metricValue = metricValue;
            id = -1;
        }
        public String toString(){
            String s = "name: " + name + ", query: " + query + ", metricValue: " + metricValue + ", id: " + id;
            return s;
        }
    };
    public MetricList(ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
    }

    public ComboBox getMetricList()
    {
        //Add metrics to Combobox
        for(int i = 0 ; i < MetricNameQueryPair.values().length; i++)
        {
            l.addItem(i+1);
            l.setItemCaption(i+1, MetricNameQueryPair.values()[i].name);
        }

        l.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        l.setImmediate(true);
        l.setNullSelectionAllowed(false);
        l.addListener(this);

        return l;

    }
    public void valueChange(ValueChangeEvent event) {
        MonitorTab monitorTab = ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab();
        termQueryBuilderList.clear();
        metricValue = null;

        // Update properties the according to the metric change
        MetricNameQueryPair metricNameQueryPair = MetricNameQueryPair.values()[((Integer)event.getProperty().getValue())-1];
        metricType = metricNameQueryPair.name;
        termQueryBuilderList.add(metricNameQueryPair.query);
        metricValue = metricNameQueryPair.metricValue;

        // Update Chart
        monitorTab.updateChart();

    }

    public ArrayList<TermQueryBuilder> getTermQueryBuilderList() {
        return termQueryBuilderList;
    }

    public void setTermQueryBuilderList(ArrayList<TermQueryBuilder> termQueryBuilderList) {
        this.termQueryBuilderList = termQueryBuilderList;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }
}

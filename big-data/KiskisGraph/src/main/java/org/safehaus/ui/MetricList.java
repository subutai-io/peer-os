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

    public MetricList(ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
    }

    public ComboBox getMetricList()
    {
        int size = 32;
        int currentCount = 1;
        for(int i = 1 ; i < size+1; i++)
            l.addItem(i);

        l.setItemCaption(currentCount++, "Bytes In");
        l.setItemCaption(currentCount++, "Bytes Out");
        l.setItemCaption(currentCount++, "CPU Aidle");
        l.setItemCaption(currentCount++, "CPU Idle");
        l.setItemCaption(currentCount++, "CPU Nice");
        l.setItemCaption(currentCount++, "CPU System");
        l.setItemCaption(currentCount++, "CPU User");
        l.setItemCaption(currentCount++, "CPU Wio");
        l.setItemCaption(currentCount++, "Load One");
        l.setItemCaption(currentCount++, "Load Five");
        l.setItemCaption(currentCount++, "Load Fifteen");
        l.setItemCaption(currentCount++, "Memory Buffers");
        l.setItemCaption(currentCount++, "Memory Cached");
        l.setItemCaption(currentCount++, "Memory Free");
        l.setItemCaption(currentCount++, "Memory Shared");
        l.setItemCaption(currentCount++, "Memory Total");
        l.setItemCaption(currentCount++, "Part Max Used");
        l.setItemCaption(currentCount++, "Packets In");
        l.setItemCaption(currentCount++, "Packets Out");
        l.setItemCaption(currentCount++, "Swap Free");
        l.setItemCaption(currentCount++, "Swap Total");
//        l.setItemCaption(currentCount++, "JVM HeapMemoryUsage_committed");
        l.removeItem(22);
        currentCount++;
        l.setItemCaption(currentCount++, "JVM HeapMemoryUsage_init");
        l.setItemCaption(currentCount++, "JVM HeapMemoryUsage_max");
        l.setItemCaption(currentCount++, "JVM HeapMemoryUsage_used");
        l.setItemCaption(currentCount++, "JVM NonHeapMemoryUsage_committed");
        l.setItemCaption(currentCount++, "JVM NonHeapMemoryUsage_init");
        l.setItemCaption(currentCount++, "JVM NonHeapMemoryUsage_max");
        l.setItemCaption(currentCount++, "JVM NonHeapMemoryUsage_used");
//        l.setItemCaption(currentCount++, "JVM PeakUsage_committed");
//        l.setItemCaption(currentCount++, "JVM PeakUsage_init");
//        l.setItemCaption(currentCount++, "JVM PeakUsage_max");
//        l.setItemCaption(currentCount++, "JVM PeakUsage_used");
        l.setItemCaption(currentCount++, "JVM DaemonThreadCount");
        l.setItemCaption(currentCount++, "JVM PeakThreadCount");
        l.setItemCaption(currentCount++, "JVM ThreadCount");
        l.setItemCaption(currentCount++, "JVM TotalStartedThreadCount");

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
        metricType = l.getItemCaption(event.getProperty().getValue());

        if(event.getProperty().toString().equalsIgnoreCase("1"))
        {
            termQueryBuilderList.add(termQuery("name", "bytes_in"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("2"))
        {
            termQueryBuilderList.add(termQuery("name", "bytes_out"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("3"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_aidle"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("4"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_idle"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("5"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_nice"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("6"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_system"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("7"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_user"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("8"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_wio"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("9"))
        {
            termQueryBuilderList.add(termQuery("name", "load_one"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("10"))
        {
            termQueryBuilderList.add(termQuery("name", "load_five"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("11"))
        {
            termQueryBuilderList.add(termQuery("name", "load_fifteen"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("12"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_buffers"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("13"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_cached"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("14"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_free"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("15"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_shared"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("16"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_total"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("17"))
        {
            termQueryBuilderList.add(termQuery("name", "part_max_used"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("18"))
        {
            termQueryBuilderList.add(termQuery("name", "pkts_in"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("19"))
        {
            termQueryBuilderList.add(termQuery("name", "pkts_out"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("20"))
        {
            termQueryBuilderList.add(termQuery("name", "swap_free"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("21"))
        {
            termQueryBuilderList.add(termQuery("name", "swap_total"));
            metricValue = "val";
        }
//        else if(event.getProperty().toString().equalsIgnoreCase("22"))
//        {
//            termQueryBuilderList.add(termQuery("name", "sun_management_emoryImpl.HeapMemoryUsage_committed".toLowerCase()));
//            metricValue = "val";
//        }
        else if(event.getProperty().toString().equalsIgnoreCase("23"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.HeapMemoryUsage_init".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("24"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.HeapMemoryUsage_max".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("25"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.HeapMemoryUsage_used".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("26"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_committed".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("27"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_init".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("28"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_max".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("29"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_MemoryImpl.NonHeapMemoryUsage_used".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("30"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_ThreadImpl.DaemonThreadCount".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("31"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_ThreadImpl.PeakThreadCount".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("32"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_ThreadImpl.ThreadCount".toLowerCase()));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("33"))
        {
            termQueryBuilderList.add(termQuery("name", "sun_management_ThreadImpl.TotalStartedThreadCount".toLowerCase()));
            metricValue = "val";
        }
        else
        {
        }
        // Update Chart according to the metric change
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

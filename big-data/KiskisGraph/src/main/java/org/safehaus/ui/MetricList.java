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

    public ComboBox getMetricList()
    {
        int size = 21;
        for(int i = 1 ; i < size+1; i++)
            l.addItem(i);

        l.setItemCaption(1, "Bytes In");
        l.setItemCaption(2, "Bytes Out");
        l.setItemCaption(3, "CPU Aidle");
        l.setItemCaption(4, "CPU Idle");
        l.setItemCaption(5, "CPU Nice");
        l.setItemCaption(6, "CPU System");
        l.setItemCaption(7, "CPU User");
        l.setItemCaption(8, "CPU Wio");
        l.setItemCaption(9, "Load One");
        l.setItemCaption(10, "Load Five");
        l.setItemCaption(11, "Load Fifteen");
        l.setItemCaption(12, "Memory Buffers");
        l.setItemCaption(13, "Memory Cached");
        l.setItemCaption(14, "Memory Free");
        l.setItemCaption(15, "Memory Shared");
        l.setItemCaption(16, "Memory Total");
        l.setItemCaption(17, "Part Max Used");
        l.setItemCaption(18, "Packets In");
        l.setItemCaption(19, "Packets Out");
        l.setItemCaption(20, "Swap Free");
        l.setItemCaption(21, "Swap Total");

        l.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        l.setImmediate(true);
        l.setNullSelectionAllowed(false);
        l.addListener(this);

        return l;

    }
    public void valueChange(ValueChangeEvent event) {
        MonitorTab monitorTab = Monitor.getMain().getMonitorTab();
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

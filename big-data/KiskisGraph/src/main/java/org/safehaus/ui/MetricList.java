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
        l.addItem(1);
        l.addItem(2);
        l.addItem(3);
        l.addItem(4);
        l.addItem(5);

        l.setItemCaption(1, "Memory Buffers");
        l.setItemCaption(2, "Memory Free");
        l.setItemCaption(3, "Memory Cached");
        l.setItemCaption(4, "CPU_System");
        l.setItemCaption(5, "CPU_Idle");

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
            termQueryBuilderList.add(termQuery("name", "mem_buffers"));
//            termQueryBuilderList.add(termQuery("collectd_type", "memory"));
//            termQueryBuilderList.add(termQuery("type_instance", "used"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("2"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_free"));
//            termQueryBuilderList.add(termQuery("collectd_type", "memory"));
//            termQueryBuilderList.add(termQuery("type_instance", "free"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("3"))
        {
            termQueryBuilderList.add(termQuery("name", "mem_cached"));
//            termQueryBuilderList.add(termQuery("collectd_type", "memory"));
//            termQueryBuilderList.add(termQuery("type_instance", "cached"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("4"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_system"));
            metricValue = "val";
        }
        else if(event.getProperty().toString().equalsIgnoreCase("5"))
        {
            termQueryBuilderList.add(termQuery("name", "cpu_idle"));
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

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
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.core.Timestamp;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class HistoryMetricList extends VerticalLayout implements
        Property.ValueChangeListener {
    Timestamp currentTime = Timestamp.getCurrentTimestamp();
    private Timestamp lastHour = Timestamp.getHoursEarlier(currentTime,1);
    private ReferenceComponent referenceComponent;

    private enum HistoryMetric {
        metric1("1 hour",1,1),
        metric2("2 hours",2,2),
        metric3("6 hours",6,3),
        metric4("1 day",24,4),
        metric5("2 days",24*2,5),
        metric6("1 week",24*7,6),
        metric7("2 weeks",24*7*2,7),
        metric8("1 month",24*7*4,8),
        ;
        String name;
        int hoursEarlier;
        int id;
        HistoryMetric(String name, int hoursEarlier, int id)
        {
            this.name = name;
            this.hoursEarlier = hoursEarlier;
            this.id = id;
        }

    };

    public HistoryMetricList(ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
    }
    public ComboBox getSampleMetricList()
    {

        ComboBox l = new ComboBox("Please select history metric");

        //Add metrics to Combobox
        for(int i = 0 ; i < HistoryMetric.values().length; i++)
        {
            l.addItem(i+1);
            l.setItemCaption(i+1, HistoryMetric.values()[i].name);
        }

        l.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        l.setImmediate(true);
        l.setNullSelectionAllowed(false);
        l.addListener(this);

        return l;

    }
    public void valueChange(Property.ValueChangeEvent event) {
        currentTime = Timestamp.getCurrentTimestamp();
        setLastHour(Timestamp.getHoursEarlier(currentTime,1));
        MonitorTab monitorTab = ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab();
        HistoryMetric historyMetric = HistoryMetric.values()[((Integer)event.getProperty().getValue())-1];

        //Change history metric according to history metric change
        setLastHour(Timestamp.getHoursEarlier(currentTime,historyMetric.hoursEarlier));
        // Update Chart according to the history metric
        monitorTab.updateChart();

        // Update Log according to the history metric
        monitorTab.updateLog();

    }

    public Timestamp getLastHour() {
        return lastHour;
    }

    public void setLastHour(Timestamp lastHour) {
        this.lastHour = lastHour;
    }
}
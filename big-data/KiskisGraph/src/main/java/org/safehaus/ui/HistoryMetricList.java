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
import org.safehaus.core.ElasticSearchAccessObject;
import org.safehaus.core.Timestamp;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class HistoryMetricList extends VerticalLayout implements
        Property.ValueChangeListener {
    ElasticSearchAccessObject ESAO = new ElasticSearchAccessObject();
    Timestamp currentTime = Timestamp.getCurrentTimestamp();
    private Timestamp lastHour = Timestamp.getHoursEarlier(currentTime,1);
    private ReferenceComponent referenceComponent;

    public HistoryMetricList(ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
    }
    public ComboBox getSampleMetricList()
    {

        ComboBox l = new ComboBox("Please select history metric");
        l.addItem(1);
        l.addItem(2);
        l.addItem(3);
        l.addItem(4);
        l.addItem(5);
        l.addItem(6);
        l.addItem(7);
        l.addItem(8);
        l.setItemCaption(1, "1 hour");
        l.setItemCaption(2, "2 hours");
        l.setItemCaption(3, "6 hours");
        l.setItemCaption(4, "1 day");
        l.setItemCaption(5, "2 days");
        l.setItemCaption(6, "5 days");
        l.setItemCaption(7, "1 week");
        l.setItemCaption(8, "2 weeks");

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
        if(event.getProperty().toString().equalsIgnoreCase("1"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,1));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("2"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,2));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("3"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,6));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("4"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,24));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("5"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,48));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("6"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,120));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("7"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,168));
        }
        else if(event.getProperty().toString().equalsIgnoreCase("8"))
        {
            setLastHour(Timestamp.getHoursEarlier(currentTime,168*2));
        }
        else
        {
        }
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
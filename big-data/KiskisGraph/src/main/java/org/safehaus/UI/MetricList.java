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

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class MetricList extends VerticalLayout implements
        Property.ValueChangeListener {
    public ComboBox getSampleMetricList()
    {

        ComboBox l = new ComboBox("Please select your metric");
        l.addItem(1);
        l.addItem(2);
        l.addItem(3);
        l.setItemCaption(1, "CPU");
        l.setItemCaption(2, "Memory");
        l.setItemCaption(3, "Network");

        l.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        l.setImmediate(true);
        l.setNullSelectionAllowed(false);
        l.addListener(this);


        return l;

    }
    public void valueChange(ValueChangeEvent event) {
        if(event.getProperty().toString().equalsIgnoreCase("1"))
        {
//            Monitor.getMain().getMonitorTab().getLogsPanel().setCaption("Logs for " + "CPU");
        }
        else if(event.getProperty().toString().equalsIgnoreCase("2"))
        {
//            Monitor.getMain().getMonitorTab().getLogsPanel().setCaption("Logs for " + "Memory");
        }
        else if(event.getProperty().toString().equalsIgnoreCase("3"))
        {
//            Monitor.getMain().getMonitorTab().getLogsPanel().setCaption("Logs for " + "Network");
        }
        else
        {
//            Monitor.getMain().getMonitorTab().getLogsPanel().setCaption("Logs for " + "Other");
        }
//        getWindow().showNotification("Selected city: " + event.getProperty());
    }

}

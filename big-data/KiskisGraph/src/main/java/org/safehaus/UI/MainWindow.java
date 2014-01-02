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

import com.vaadin.ui.*;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class MainWindow extends Window{
//    private GridLayout grid = new GridLayout(2, 2);
    private HorizontalSplitPanel horiz;
    private Panel leftPanel;
    private MonitorTab monitorTab;
    public MainWindow()
    {
        monitorTab = new MonitorTab();
        horiz = new HorizontalSplitPanel();
        getMonitorTab().setWidth("100%");
        getMonitorTab().setHeight("100%");

        horiz.setSplitPosition(20); // percent
        horiz.setWidth("100%");
//        horiz.setHeight("1050");
        leftPanel = new Panel();

//        leftPanel.addComponent(Host.getSampleHostTree());
        leftPanel.addComponent(Host.getRealHostTree());

        horiz.addComponent(Host.getRealHostTree());
//        horiz.addComponent(leftPanel);

        horiz.addComponent(getMonitorTab());
        setContent(horiz);
//        addComponent(horiz);
    }

    public MonitorTab getMonitorTab() {
        return monitorTab;
    }

    public void setMonitorTab(MonitorTab monitorTab) {
        this.monitorTab = monitorTab;
    }
}

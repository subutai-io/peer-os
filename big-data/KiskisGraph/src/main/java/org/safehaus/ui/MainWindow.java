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

import com.vaadin.ui.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class MainWindow extends Window{
    private HorizontalSplitPanel horiz;
    private Host hosts;
    GridLayout layout;
    int temp = 0;
    Tree hostTree;
    Button refreshHostsButton;
    Button refreshButton;
    private MonitorTab monitorTab;
    private Logger logger = Logger.getLogger("MainWindowLogger");
    private ReferenceComponent referenceComponent;
    public MainWindow()
    {

    }
    public void initialize(ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
        refreshHostsButton = new Button("Refresh Hosts");
        refreshButton = new Button("Refresh UI");

        horiz = new HorizontalSplitPanel();
        horiz.setSplitPosition(15); // percent
        horiz.setWidth("100%");

        addInitalUIComponents();
        setContent(horiz);

        refreshHostsButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                logger.log(Level.INFO, "Updating host list!");
                layout.removeComponent(hostTree);
                refreshHostsButton.setEnabled(false);
                updateHosts();
                refreshHostsButton.setEnabled(true);
            }
        });
        refreshButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                horiz.removeAllComponents();
                refreshButton.setEnabled(false);
                addInitalUIComponents();
                refreshButton.setEnabled(true);
                requestRepaintAll();
            }
        });


    }
    public void addInitalUIComponents()
    {
        monitorTab = new MonitorTab(referenceComponent);
        getMonitorTab().setWidth("100%");
        layout = new GridLayout(2,2);
        layout.setHeight("100%");
        setHosts(new Host(referenceComponent));
        updateHosts();
        layout.addComponent(refreshHostsButton, 0 , 1);
        layout.addComponent(refreshButton, 1 , 1);
        layout.setComponentAlignment(refreshHostsButton, Alignment.BOTTOM_CENTER);
        layout.setComponentAlignment(refreshButton, Alignment.BOTTOM_CENTER);

        horiz.addComponent(layout);
        horiz.addComponent(getMonitorTab());

    }
    private void updateHosts()
    {
        hostTree = getHosts().getRealHostTree();
        layout.addComponent(hostTree, 0,0);
    }
    private void updateSampleHosts()
    {
        hostTree = getHosts().getSampleHosts();
        layout.addComponent(hostTree);
    }

    public MonitorTab getMonitorTab() {
        return monitorTab;
    }

    public void setMonitorTab(MonitorTab monitorTab) {
        this.monitorTab = monitorTab;
    }

    public Host getHosts() {
        return hosts;
    }

    public void setHosts(Host hosts) {
        this.hosts = hosts;
    }

    public ReferenceComponent getReferenceComponent() {
        return referenceComponent;
    }

    public void setReferenceComponent(ReferenceComponent referenceComponent) {
        this.referenceComponent = referenceComponent;
    }
}

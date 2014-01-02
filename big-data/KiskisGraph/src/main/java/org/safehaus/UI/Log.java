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

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.safehaus.Core.ElasticSearchAccessObject;
import org.safehaus.Core.LogResponse;
import org.safehaus.Core.Timestamp;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.paged.PagedFilterTable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by skardan on 1/2/14.
 */
public class Log extends VerticalLayout {
    private IndexedContainer indexedContainer;
    private FilterTable normalFilterTable;
    private PagedFilterTable<IndexedContainer> pagedFilterTable;

    public Log(){
        TabSheet tabSheet = new TabSheet();
        indexedContainer =  new IndexedContainer();

        normalFilterTable = buildFilterTable();
        pagedFilterTable = buildPagedFilterTable();


        Component tab1 = buildNormalTableTab(getNormalFilterTable());
        Component tab2 = buildPagedTableTab(getPagedFilterTable());
        tabSheet.addTab(tab1,"Normal");
        tabSheet.addTab(tab2,"Paged");
        addComponent(tabSheet);
    }

    private Component buildNormalTableTab(final FilterTable normalFilterTable) {
        final Label placeHolder = new Label("");
        placeHolder.setSizeFull();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(normalFilterTable);
        mainLayout.setExpandRatio(normalFilterTable, 2);
        mainLayout.addComponent(buildButtons(normalFilterTable));
        normalFilterTable.setColumnCollapsed("version", true);
        normalFilterTable.setColumnCollapsed("type", true);

        Button test = new Button("Remove/Add Table");
        test.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (mainLayout.getComponent(0) == normalFilterTable) {
                    mainLayout.replaceComponent(normalFilterTable, placeHolder);
                    mainLayout.setExpandRatio(placeHolder, 2);
                } else {
                    mainLayout.replaceComponent(placeHolder, normalFilterTable);
                    mainLayout.setExpandRatio(normalFilterTable, 2);
                }
            }
        });
        mainLayout.addComponent(test);

        Panel p = new Panel();
        p.setStyleName(Reindeer.PANEL_LIGHT);
        p.setSizeFull();
        p.setContent(mainLayout);

        return p;
    }

    private Component buildPagedTableTab(PagedFilterTable<IndexedContainer> pagedFilterTable) {
        //PagedFilterTable<IndexedContainer> pagedFilterTable = buildPagedFilterTable();
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(pagedFilterTable);
        mainLayout.addComponent(pagedFilterTable.createControls());
        mainLayout.addComponent(buildButtons(pagedFilterTable));
        pagedFilterTable.setColumnCollapsed("version", true);
        pagedFilterTable.setColumnCollapsed("type", true);
        pagedFilterTable.setPageLength(15);
        return mainLayout;
    }

    private FilterTable buildFilterTable() {
        FilterTable filterTable = new FilterTable();
        filterTable.setSizeFull();
        filterTable.setFilterBarVisible(true);
        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(true);
        filterTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
        filterTable.setColumnCollapsingAllowed(true);
        filterTable.setColumnCollapsed("version", true);
        filterTable.setColumnCollapsed("type", true);
        filterTable.setColumnReorderingAllowed(true);
        filterTable.setContainerDataSource(buildContainer());
        filterTable.setVisibleColumns(new String[] { "message", "path", "version", "type", "date", "host" });
        return filterTable;
    }



    private PagedFilterTable<IndexedContainer> buildPagedFilterTable() {
        PagedFilterTable<IndexedContainer> filterTable = new PagedFilterTable<IndexedContainer>();

        filterTable.setWidth("100%");
        filterTable.setFilterBarVisible(true);
        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(true);
        filterTable.setColumnCollapsingAllowed(true);
        filterTable.setColumnReorderingAllowed(true);
        filterTable.setContainerDataSource(buildContainer());
        filterTable.setVisibleColumns(new String[]{"message", "path", "version", "type", "date", "host"});
        return filterTable;
    }

    public Container buildContainer() {
        indexedContainer.addContainerProperty("message", String.class, null);
        indexedContainer.addContainerProperty("path", String.class, null);
        indexedContainer.addContainerProperty("version", Integer.class, null);
        indexedContainer.addContainerProperty("type", String.class, null);
        indexedContainer.addContainerProperty("date", java.sql.Timestamp.class, null);
        indexedContainer.addContainerProperty("host", String.class, null);

        return indexedContainer;
    }

    public void fillTable(ArrayList<LogResponse> tableData, int lastIndex){
        if(lastIndex == -1)
            lastIndex = 0;

        System.out.println("Last Index of Log in fillTable: " + lastIndex);
        for(int i=0; i<tableData.size(); i++){
            try{
                //System.out.println("new item id is : " + cont.addItem(lastIndex+i));
                indexedContainer.addItemAt((lastIndex + i), (lastIndex + i));
                if(indexedContainer.getItem((lastIndex+i)) == null){
                    System.out.println("item is null ");
                }
                indexedContainer.getContainerProperty((lastIndex+i), "message").setValue(tableData.get(i).getMessage());
                indexedContainer.getContainerProperty((lastIndex+i), "path").setValue(tableData.get(i).getPath());
                indexedContainer.getContainerProperty((lastIndex+i), "version").setValue(tableData.get(i).getVersion());
                indexedContainer.getContainerProperty((lastIndex+i), "type").setValue(tableData.get(i).getType());
                indexedContainer.getContainerProperty((lastIndex+i), "date").setValue(new java.sql.Timestamp(strDateToUnixTimestamp(convertTimestamp(tableData.get(i).getTimestamp()))));
                indexedContainer.getContainerProperty((lastIndex+i), "host").setValue(tableData.get(i).getHost());
            }catch (Exception ex){
                ex.printStackTrace();
                //System.err.println("Message field does not exists !!!");
                System.out.println(ex.getMessage());
                System.out.println("-----------------------------------");
                System.out.println("host : " + tableData.get(i).getHost());
                System.out.println("path : " + tableData.get(i).getPath());
                System.out.println("message : " + tableData.get(i).getMessage());
                System.out.println("type : " + tableData.get(i).getType());
                System.out.println("date : " + tableData.get(i).getTimestamp());
                System.out.println("----------------------------------");
            }
        }
    }

    private Component buildButtons(final FilterTable relatedFilterTable) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setHeight(null);
        buttonLayout.setWidth("100%");
        buttonLayout.setSpacing(true);

        Label hideFilters = new Label("Show Filters:");
        hideFilters.setSizeUndefined();
        buttonLayout.addComponent(hideFilters);
        buttonLayout.setComponentAlignment(hideFilters, Alignment.MIDDLE_LEFT);

        for (Object propId : relatedFilterTable.getContainerPropertyIds()) {
            Component t = createToggle(relatedFilterTable, propId);
            buttonLayout.addComponent(t);
            buttonLayout.setComponentAlignment(t, Alignment.MIDDLE_LEFT);
        }

        CheckBox showFilters = new CheckBox("Toggle Filter Bar visibility");
        showFilters.setValue(relatedFilterTable.isFilterBarVisible());
        showFilters.setImmediate(true);
        showFilters.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                relatedFilterTable.setFilterBarVisible((Boolean) event
                        .getProperty().getValue());

            }
        });
        buttonLayout.addComponent(showFilters);
        buttonLayout.setComponentAlignment(showFilters, Alignment.MIDDLE_RIGHT);
        buttonLayout.setExpandRatio(showFilters, 2);

        Button reset = new Button("Reset");
        reset.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                relatedFilterTable.resetFilters();
            }
        });
        buttonLayout.addComponent(reset);

        return buttonLayout;
    }

    public String convertTimestamp(String date){
        return date.substring(0,10) + " " + date.substring(11, (date.length()-1));
    }

    private static long strDateToUnixTimestamp(String dt) {
        DateFormat formatter;
        Date date = null;
        long unixtime;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            date = formatter.parse(dt);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        unixtime = date.getTime();
        return unixtime;
    }


    private Component createToggle(final FilterTable relatedFilterTable,
                                   final Object propId) {
        CheckBox toggle = new CheckBox(propId.toString());
        toggle.setValue(relatedFilterTable.isFilterFieldVisible(propId));
        toggle.setImmediate(true);
        toggle.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                relatedFilterTable.setFilterFieldVisible(propId,
                        !relatedFilterTable.isFilterFieldVisible(propId));
            }
        });
        return toggle;
    }

    public IndexedContainer getIndexedContainer() {
        return indexedContainer;
    }

    public void setIndexedContainer(IndexedContainer indexedContainer) {
        this.indexedContainer = indexedContainer;
    }

    public FilterTable getNormalFilterTable() {
        return normalFilterTable;
    }

    public void setNormalFilterTable(FilterTable normalFilterTable) {
        this.normalFilterTable = normalFilterTable;
    }

    public PagedFilterTable<IndexedContainer> getPagedFilterTable() {
        return pagedFilterTable;
    }

    public void setPagedFilterTable(PagedFilterTable<IndexedContainer> pagedFilterTable) {
        this.pagedFilterTable = pagedFilterTable;
    }
}
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

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.safehaus.core.LogResponse;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.paged.PagedFilterTable;
import org.vaadin.overlay.CustomOverlay;
import org.vaadin.overlay.ImageOverlay;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by skardan on 1/2/14.
 */
public class Log extends VerticalLayout {
    private IndexedContainer indexedContainer;
    private FilterTable normalFilterTable;
    private PagedFilterTable<IndexedContainer> pagedFilterTable;
    private Logger logger = Logger.getLogger("LogLogger");

    // Create a dynamically updating content for the popup
    public class PopupTextField implements PopupView.Content {

        private TextField tf = new TextField("Edit me");
        private VerticalLayout root = new VerticalLayout();

        public PopupTextField() {
            root.setSizeUndefined();
            root.setSpacing(true);
            root.setMargin(true);
            root.addComponent(new Label(
                    "The changes made to any components inside the popup are reflected automatically when the popup is closed, but you might want to provide explicit action buttons for the user, like \"Save\" or \"Close\"."));

            root.addComponent(tf);
            tf.setValue("Initial dynamic content");
            tf.setWidth("300px");
        }

        public String getMinimizedValueAsHTML() {
            return tf.getValue().toString();
        }

        public Component getPopupComponent() {
            return root;
        }
    };

    public Log(){
        TabSheet tabSheet = new TabSheet();
        indexedContainer =  new IndexedContainer();

        normalFilterTable = buildFilterTable();
        pagedFilterTable = buildPagedFilterTable();

        Component tab1 = buildNormalTableTab(getNormalFilterTable());
        Component tab2 = buildPagedTableTab(getPagedFilterTable());
        tabSheet.addTab(tab1,"Normal",null);
        tabSheet.addTab(tab2,"Paged",null);
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
        filterTable.setVisibleColumns(new String[]{"message", "path", "version", "type", "date", "host"});
        filterTable.setColumnWidth("message", 800);
        filterTable.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                String a = event.getProperty().getValue().toString();
                a = a.substring(1, a.length()-1);
                int index = Integer.parseInt(a);

                String text = parseMessage(indexedContainer.getItem(index).toString());
                String[] parts = text.split("%");

                String message = parts[0];
                String path = parts[1];
                String version = parts[2];
                String type = parts[3];
                String date = parts[4];
                String host = parts[5];


                String notice = "<table  style=\"font-size:12px;color:#000000;\" >\n" +
                        "<tr>\n" +
                        "<td> Message    </td>  <td> &nbsp;&nbsp;: " + message + "</td>\n" +
                        "</tr>\n" +
                        "<tr>\n" +
                        "<td> Path       </td>  <td> &nbsp;&nbsp;: " + path    + "</td>\n" +
                        "</tr>  \n" +
                        "<tr>\n" +
                        "<td> Version    </td>  <td> &nbsp;&nbsp;: " + version + "</td>\n" +
                        "</tr>  \n" +
                        "<tr>\n" +
                        "<td> Type       </td>  <td> &nbsp;&nbsp;: " + type    + "</td>\n" +
                        "</tr>  \n" +
                        "<tr>\n" +
                        "<td> Timestamp  </td>  <td> &nbsp;&nbsp;: " + date    + "</td>\n" +
                        "</tr>  \n" +
                        "<tr>\n" +
                        "<td> Host       </td>  <td> &nbsp;&nbsp;: " + host    + "</td>\n" +
                        "</tr>  \n" +
                        "</table> ";

                Window.Notification notification = new Window.Notification(notice);
                notification.setPosition(Window.Notification.POSITION_CENTERED_BOTTOM);
                getWindow().showNotification(notification);

                /*
                TextArea area = new TextArea();
                String result = parseMessage(indexedContainer.getItem(index).toString());
                area.setWidth("800px");
                area.setHeight("120px");
                area.setWordwrap(false);

                String[] parts = result.split("%");
                for (int i=0; i<parts.length; i++){
                    System.out.println(parts[i]);
                }
                area.setValue(parts[0] + "\n" + parts[1] + "\n" + parts[2] + "\n" + parts[3] + "\n" + parts[4] + "\n" + parts[5]);

                PopupView popup = new PopupView(null, area);
                getComponent(popup.getComponentCount()).getWindow().setPositionX(400);
                getComponent(popup.getComponentCount()).getWindow().setPositionY(400);
                popup.setPopupVisible(true);
                addComponent(popup);
                */
            }
        });
        return filterTable;
    }

    public static String parseMessage(String message){
        String result;
        String host = message.substring(message.lastIndexOf(' ')+1, message.length());
        message = message.substring(0, message.lastIndexOf(' '));
        int tmp = message.lastIndexOf(' ');
        String t = message.substring(0, tmp);
        int tmp1 = t.lastIndexOf(' ');

        String date = message.substring(tmp1+1, message.length());
        message = message.substring(0, tmp1);
        int tmp2 = message.lastIndexOf(' ');

        String type = message.substring(tmp2+1, message.length());
        message = message.substring(0, tmp2);
        int tmp3 = message.lastIndexOf(' ');

        String version = message.substring(tmp3+1, message.length());
        message = message.substring(0, tmp3);
        int tmp4 = message.lastIndexOf(' ');

        String path = message.substring(tmp4+1, message.length());
        message = message.substring(0, tmp4);

        result= message + "%" + path + "%" + version + "%" + type + "%" + date + "%" + host;

        return result;
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
        if(tableData == null | tableData.size() == 0)
            return;
        System.out.println("New log data size: " + tableData.size());
        for(int i=0; i<tableData.size(); i++){

            //System.out.println("new item id is : " + cont.addItem(lastIndex+i));
/*

            LogResponse log = new LogResponse(tableData.get(i).getMessage(),
                    tableData.get(i).getTimestamp(),
                    tableData.get(i).getVersion(),
                    tableData.get(i).getType(),
                    tableData.get(i).getHost(),
                    tableData.get(i).getPath());

            VerticalLayout cellLayout = new VerticalLayout();
            cellLayout.addComponent(new Label(log.getMessage()));
            cellLayout.addComponent(new Label(log.getVersion()));
            cellLayout.addComponent(new Label(log.getType()));
            cellLayout.addComponent(new Label(log.getHost()));
            cellLayout.addComponent(new Label(log.getPath()));

            cellLayout.addListener(new LayoutEvents.LayoutClickListener() {
                public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                    System.out.println("hey");
                }
            });

            indexedContainer.addItemAt((lastIndex + i), cellLayout);

*/

            indexedContainer.addItemAt((lastIndex + i), (lastIndex + i));

            if(indexedContainer.getItem((lastIndex+i)) == null){
                System.out.println("item is null ");
                indexedContainer.removeItem(lastIndex+i);
            }
            else
            {
                indexedContainer.getContainerProperty((lastIndex+i), "message").setValue(tableData.get(i).getMessage());
                indexedContainer.getContainerProperty((lastIndex+i), "path").setValue(tableData.get(i).getPath());
                indexedContainer.getContainerProperty((lastIndex+i), "version").setValue(tableData.get(i).getVersion());
                indexedContainer.getContainerProperty((lastIndex+i), "type").setValue(tableData.get(i).getType());
                indexedContainer.getContainerProperty((lastIndex+i), "date").setValue(new java.sql.Timestamp(strDateToUnixTimestamp(convertTimestamp(tableData.get(i).getTimestamp()))));
                indexedContainer.getContainerProperty((lastIndex+i), "host").setValue(tableData.get(i).getHost());
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
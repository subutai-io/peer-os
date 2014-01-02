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

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.GradientColor;
import com.vaadin.addon.charts.model.style.SolidColor;
import org.elasticsearch.action.search.SearchResponse;
import org.safehaus.Core.MemoryResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class Statistic {
    private DataSeries memoryChartDataSeries;

    public Statistic()
    {
        memoryChartDataSeries = new DataSeries();
    }

    public Chart getMemoryChart(SearchResponse response)
    {
        Chart chart = new Chart();
        chart.setHeight("300px");
        chart.setWidth("100%");

        Configuration configuration = new Configuration();
        configuration.getChart().setZoomType(ZoomType.X);
        configuration.getChart().setSpacingRight(20);

        configuration.getTitle().setText("Last Hour Memory Free");

        String title = "Click and drag in the plot area to zoom in";
        configuration.getSubTitle().setText(title);

        configuration.getxAxis().setType(AxisType.DATETIME);
        // configuration.getxAxis().getLabels().setFormatter();
        configuration.getxAxis().setTitle(new Title("Hour"));

        configuration.getLegend().setEnabled(false);

        Axis yAxis = configuration.getyAxis();
        yAxis.setTitle(new Title("Memory Free"));
        yAxis.setStartOnTick(false);
        yAxis.setShowFirstLabel(false);

        configuration.getTooltip().setShared(true);

        PlotOptionsArea plotOptions = new PlotOptionsArea();

        GradientColor fillColor = GradientColor.createLinear(0, 0, 0, 1);
        fillColor.addColorStop(0, new SolidColor("#4572A7"));
        fillColor.addColorStop(1, new SolidColor(2, 0, 0, 0));
        plotOptions.setFillColor(fillColor);

        plotOptions.setLineWidth(1);
        plotOptions.setShadow(false);

        Marker marker = new Marker();
        marker.setEnabled(false);
        State hoverState = new State(true);
        hoverState.setRadius(5);
        MarkerStates states = new MarkerStates(hoverState);
        marker.setStates(states);

        State hoverStateForArea = new State(true);
        hoverState.setLineWidth(1);

        plotOptions.setStates(new States(hoverStateForArea));
        plotOptions.setMarker(marker);
        plotOptions.setShadow(false);
        configuration.setPlotOptions(plotOptions);


        PlotOptionsArea options = new PlotOptionsArea();
        memoryChartDataSeries.setPlotOptions(options);
        memoryChartDataSeries.setName("Memory Free");

//        try {
//            options.setPointStart(df.parse("2006/01/02").getTime());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        MemoryResponse memoryResponse = new MemoryResponse(response);

        addData(memoryResponse);
        configuration.setSeries(memoryChartDataSeries);

        chart.drawChart(configuration);

        return chart;
    }
    public static Date d(String dateString) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("EET"));
        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void addData(MemoryResponse memoryResponse)
    {
        for (int i = 0; i < memoryResponse.getValues().length; i++) {
            DataSeriesItem item = new DataSeriesItem(d(memoryResponse.getTimestamps()[i].toString().replace("T"," ")),
                    memoryResponse.getValues()[i]);
            memoryChartDataSeries.add(item);
        }

    }
    public void clearData()
    {
        memoryChartDataSeries.clear();
        System.out.println("Cleared Data:" + memoryChartDataSeries.getData());
    }


}

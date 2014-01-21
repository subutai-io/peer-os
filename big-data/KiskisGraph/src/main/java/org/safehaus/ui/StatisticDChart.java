///*
// *  Licensed to the Apache Software Foundation (ASF) under one
// *  or more contributor license agreements.  See the NOTICE file
// *  distributed with this work for additional information
// *  regarding copyright ownership.  The ASF licenses this file
// *  to you under the Apache License, Version 2.0 (the
// *  "License"); you may not use this file except in compliance
// *  with the License.  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// *
// */
//package org.safehaus.ui;
//
//import org.dussan.vaadin.dcharts.DCharts;
//import org.dussan.vaadin.dcharts.base.elements.XYaxis;
//import org.dussan.vaadin.dcharts.data.DataSeries;
//import org.dussan.vaadin.dcharts.metadata.XYaxes;
//import org.dussan.vaadin.dcharts.metadata.renderers.LabelRenderers;
//import org.dussan.vaadin.dcharts.options.Axes;
//import org.dussan.vaadin.dcharts.options.AxesDefaults;
//import org.dussan.vaadin.dcharts.options.Options;
//import org.dussan.vaadin.dcharts.options.Title;
//import org.elasticsearch.action.search.SearchResponse;
//import org.safehaus.core.StatisticResponse;
//
///**
// * ...
// *
// * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
// * @version $Rev$
// */
//public class StatisticDChart extends DCharts {
//    private DataSeries dataSeries;
////    private Configuration configuration;
//    StatisticResponse statisticResponse;
//    String yAxisTitle= "";
//    String yAxisTitleWithUnit = "";
//    private ReferenceComponent referenceComponent;
//
//    public StatisticDChart(ReferenceComponent referenceComponent, SearchResponse response)
//    {
//        this.referenceComponent = referenceComponent;
//        dataSeries = new DataSeries();
////        configuration = this.getConfiguration();
//        MonitorTab monitorTab;
//        if(referenceComponent.getApplication() != null && ((Monitor)referenceComponent.getApplication()).getMain().getMonitorTab() != null)
//        {
//            monitorTab = ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab();
//            statisticResponse =  new StatisticResponse(response, monitorTab.getMetricList().getMetricValue());
//            yAxisTitle = monitorTab.getMetricList().getMetricType();
//            yAxisTitleWithUnit = yAxisTitle + " (" +  statisticResponse.getUnits() +")";
//        }
//        else
//            statisticResponse = null;
//        if(statisticResponse != null)
//        {
////            addData(statisticResponse);
//        }
////        configuration.setSeries(dataSeries);
//    }
//    public StatisticDChart getDefaultChart()
//    {
//        Title title = new Title("Plot With Options");
//
//        AxesDefaults axesDefaults = new AxesDefaults()
//                .setLabelRenderer(LabelRenderers.CANVAS);
//
//        Axes axes = new Axes()
//                .addAxis(
//                        new XYaxis()
//                                .setLabel("X Axis")
//                                .setPad(0))
//                .addAxis(
//                        new XYaxis(XYaxes.Y)
//                                .setLabel("Y Axis"));
//
//        Options options = new Options()
//                .setAxesDefaults(axesDefaults)
//                .setAxes(axes);
//
//        DataSeries dataSeries = new DataSeries()
//                .add(3, 7, 9, 1, 4, 6, 8, 2, 5);
//
//        setDataSeries(dataSeries);
//        setOptions(options);
//        show();
////        DCharts chart = new DCharts()
////                .setDataSeries(dataSeries)
////                .setOptions(options)
////                .show();
//
//        return this;
//    }
////    public static Date d(String dateString) {
////        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////        df.setTimeZone(TimeZone.getTimeZone("EET"));
////        try {
////            return df.parse(dateString);
////        } catch (ParseException e) {
////            throw new RuntimeException(e);
////        }
////    }
////    public void addData(StatisticResponse memoryResponse)
////    {
////        for (int i = 0; i < memoryResponse.getValues().length; i++) {
////            DataSeriesItem item = new DataSeriesItem(d(memoryResponse.getTimestamps()[i].toString().replace("T"," ")),
////                    memoryResponse.getValues()[i]);
////            dataSeries.add(item);
////        }
////
////    }
////    public static void simpleReduce(DataSeries series, int pixels) {
////        if(series.size() < 500)
////            return;
////        DataSeriesItem first = series.get(0);
////        DataSeriesItem last = series.get(series.size() - 1);
////        ArrayList reducedDataSet = new ArrayList();
////        if (first.getX() != null) {
////            // xy pairs
////            double startX = first.getX().doubleValue();
////            double endX = last.getX().doubleValue();
////            double minDistance = (endX - startX) / pixels;
////            reducedDataSet.add(first);
////            double lastPoint = first.getX().doubleValue();
////            for (int i = 0; i < series.size(); i++) {
////                DataSeriesItem item = series.get(i);
////                if (item.getX().doubleValue() - lastPoint > minDistance) {
////                    reducedDataSet.add(item);
////                    lastPoint = item.getX().doubleValue();
////                }
////            }
////            series.setData(reducedDataSet);
////        } else {
////            // interval data
////            int k = series.size() / pixels;
////            if (k > 1) {
////                for (int i = 0; i < series.size(); i++) {
////                    if (i % k == 0) {
////                        DataSeriesItem item = series.get(i);
////                        reducedDataSet.add(item);
////                    }
////                }
////                series.setData(reducedDataSet);
////            }
////        }
////    }
//}

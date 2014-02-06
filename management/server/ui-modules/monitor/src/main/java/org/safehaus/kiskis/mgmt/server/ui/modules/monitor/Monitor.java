package org.safehaus.kiskis.mgmt.server.ui.modules.monitor;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.ArrayList;
import java.util.List;

public class Monitor implements Module {

    public static final String MODULE_NAME = "Monitor";

    public static class ModuleComponent extends CustomComponent {

    }

    @Override
    public String getName() {
        return org.safehaus.kiskis.mgmt.server.ui.modules.monitor.Monitor.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return getChart();
    }

    protected Component getChart() {
        Chart chart = new Chart(ChartType.BAR);

        Configuration conf = chart.getConfiguration();

        conf.setTitle("Historic World Population by Region");
        conf.setSubTitle("Source: Wikipedia.org");

        XAxis x = new XAxis();
        x.setCategories("Africa", "America", "Asia", "Europe", "Oceania");
        x.setTitle((String) null);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setMin(0);
        Title title = new Title("Population (millions)");
        title.setVerticalAlign(VerticalAlign.HIGH);
        y.setTitle(title);
        conf.addyAxis(y);

        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("this.series.name +': '+ this.y +' millions'");
        conf.setTooltip(tooltip);

        PlotOptionsBar plot = new PlotOptionsBar();
        plot.setDataLabels(new Labels(true));
        conf.setPlotOptions(plot);

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setHorizontalAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(-100);
        legend.setY(100);
        legend.setFloating(true);
        legend.setBorderWidth(1);
        legend.setBackgroundColor("#FFFFFF");
        legend.setShadow(true);
        conf.setLegend(legend);

        conf.disableCredits();

        List<Series> series = new ArrayList<Series>();
        series.add(new ListSeries("Year 1800", 107, 31, 635, 203, 2));
        series.add(new ListSeries("Year 1900", 133, 156, 947, 408, 6));
        series.add(new ListSeries("Year 2008", 973, 914, 4054, 732, 34));
        conf.setSeries(series);

        chart.drawChart(conf);

        return chart;
    }

}

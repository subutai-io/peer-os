package org.safehaus.subutai.core.monitor.ui;


import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.ui.util.FileUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


class Chart {

	private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

	private final int maxSize;
//    private final JavaScript javaScript;


	Chart(int maxSize) {
		this.maxSize = maxSize;
//        javaScript = new JavaScript( window );
		loadScripts();
	}


	private void loadScripts() {
//        javaScript.loadFile( "js/jquery.min.js" );
//        javaScript.loadFile( "js/jquery.flot.min.js" );
//        javaScript.loadFile( "js/jquery.flot.time.min.js" );
		com.vaadin.ui.JavaScript.getCurrent().execute("js/jquery.min.js");
		com.vaadin.ui.JavaScript.getCurrent().execute("js/jquery.flot.min.js");
		com.vaadin.ui.JavaScript.getCurrent().execute("js/jquery.flot.time.min.js");
	}


	void load(String host, Metric metric, Map<Date, Double> values) {

		String data = toPoints(values);
		String label = String.format("%s for %s", metric.toString(), host);

		String chart = CHART_TEMPLATE.replace("$label", label)
				.replace("$yTitle", metric.getUnit())
				.replace("$data", data);

//        javaScript.execute( chart );
		com.vaadin.ui.JavaScript.getCurrent().execute(chart);
	}


	private String toPoints(Map<Date, Double> values) {

		String str = "";
		int i = 0;

		for (Date date : values.keySet()) {
			if (!str.isEmpty()) {
				str += ", ";
			}

			str += String.format("[Date.parse('%s'), %s ]", DATE_FORMAT.format(date), values.get(date));
			i++;

			if (i > maxSize) {
				break;
			}
		}

		return String.format("[%s]", str);
	}
}

package org.safehaus.subutai.core.monitor.impl;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class MonitorImpl implements Monitor {

	private final static Logger LOG = LoggerFactory.getLogger(MonitorImpl.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String QUERY = FileUtil.getContent("elasticsearch/query.json");


	/**
	 * The method uses the simplest way to get the data for all the metrics. In terms of performance this is not
	 * the best way but the speed should not be an issue for near future. Further optimization should be
	 * done with rewriting the query to elasticsearch. See: query.json.
	 */
	@Override
	public Map<Metric, Map<Date, Double>> getDataForAllMetrics(String host, Date startDate, Date endDate) {

		HashMap<Metric, Map<Date, Double>> data = new HashMap<>();

		for (Metric metric : Metric.values()) {
			Map<Date, Double> metricData = getData(host, metric, startDate, endDate);
			data.put(metric, metricData);
		}

		return data;
	}


	@Override
	public Map<Date, Double> getData(String host, Metric metric, Date startDate, Date endDate) {
		LOG.info("host: {}, metric: {}, startDate: {}, endDate: {}", host, metric, startDate, endDate);

		Map<Date, Double> data = Collections.emptyMap();

		try {
			data = execute(host, metric, startDate, endDate);
		} catch (Exception e) {
			LOG.error("Error while executing query: ", e);
		}

		return data;
	}


	private Map<Date, Double> execute(String host, Metric metric, Date startDate, Date endDate) throws Exception {

		String query = QUERY.replace("$host", host).replace("$metricName", metric.name().toLowerCase())
				.replace("$startDate", dateToStr(startDate))
				.replace("$endDate", dateToStr(endDate));

		LOG.debug("query: {}", query);

		String response = HttpPost.execute(query);
		List<JsonNode> nodes = toNodes(response);

		LOG.info("nodes count: {}", nodes.size());

		// Reversing the list b/c the query returns the data in desc order (to get the latest values first).
		Collections.reverse(nodes);

		return toMap(nodes);
	}


	private static List<JsonNode> toNodes(String response) throws IOException {

		JsonNode json = OBJECT_MAPPER.readTree(response);
		JsonNode hits = json.get("hits").get("hits");

		ArrayList<JsonNode> nodes = new ArrayList<>();

		for (int i = 0; i < hits.size(); i++) {
			JsonNode node = hits.get(i).get("_source");
			nodes.add(node);

			LOG.debug("node: {}", node);
		}

		return nodes;
	}


	private static Map<Date, Double> toMap(List<JsonNode> nodes) {

		Map<Date, Double> values = new TreeMap<>();

		for (JsonNode node : nodes) {
			Date date = strToDate(node.get("@timestamp").asText());
			double value = node.get("val").asDouble();
			values.put(date, value);
		}

		return values;
	}


	private static Date strToDate(String dateStr) {

		String target = dateStr.replace("T", " ").replace("Z", "");
		Date date = null;

		try {
			date = DATE_FORMAT.parse(target);
		} catch (ParseException e) {
			LOG.error("Error while parsing time: ", e);
		}

		return date;
	}


	private static String dateToStr(Date date) {
		return DATE_FORMAT.format(date).replace(" ", "T");
	}
}

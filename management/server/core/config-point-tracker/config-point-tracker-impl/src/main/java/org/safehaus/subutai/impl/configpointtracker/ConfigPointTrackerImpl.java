package org.safehaus.subutai.impl.configpointtracker;


import org.safehaus.subutai.api.configpointtracker.ConfigPointTracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class ConfigPointTrackerImpl implements ConfigPointTracker {

	// <templateName, configPoints>
	private final HashMap<String, Set<String>> configPoints = new HashMap<>();


	@Override
	public void add(String templateName, String... configPaths) {

		Set<String> points = configPoints.get(templateName);

		if (points == null) {
			points = new HashSet<>();
			configPoints.put(templateName, points);
		}

		for (String path : configPaths) {
			points.add(path);
		}
	}


	@Override
	public void remove(String templateName, String... configPaths) {

		Set<String> points = configPoints.get(templateName);

		if (points == null) {
			return;
		}

		for (String path : configPaths) {
			points.remove(path);
		}
	}


	@Override
	public Set<String> get(String templateName) {
		return configPoints.get(templateName);
	}
}

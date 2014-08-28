package org.safehaus.subutai.api.hive.query;

import org.safehaus.subutai.shared.protocol.ConfigBase;

public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Hive Query";

	private String name;
	private String query;
	private String description;

	public Config() {
	}

	public Config(String name, String query, String description) {
		this.name = name;
		this.query = query;
		this.description = description;
	}

	@Override
	public String getClusterName() {
		return name;
	}

	@Override
	public String getProductName() {
		return PRODUCT_KEY;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Config{" +
				"name='" + name + '\'' +
				", query='" + query + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}

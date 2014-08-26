package org.safehaus.subutai.impl.hive;

import org.safehaus.subutai.shared.protocol.Agent;

public class Commands {

	private static final String EXEC_PROFILE = ". /etc/profile";

	public static String make(CommandType type, Product product) {
		StringBuilder sb;
		switch (type) {
			case LIST:
				return "dpkg -l | grep '^ii' | grep ksks";
			case INSTALL:
			case PURGE:
				sb = new StringBuilder("apt-get --force-yes --assume-yes ");
				sb.append(type.toString().toLowerCase()).append(" ");
				sb.append(product.getPackageName());
				break;
			case STATUS:
			case START:
			case STOP:
			case RESTART:
				sb = new StringBuilder();
				if (product.isProfileScriptRun())
					sb.append(EXEC_PROFILE).append(" && ");
				sb.append("service ").append(product.getServiceName());
				sb.append(" ").append(type.toString().toLowerCase());
				break;
			default:
				throw new AssertionError(type.name());
		}
		return sb != null ? sb.toString() : null;
	}

	public static String configureHiveServer(String host) {
		return ". /etc/profile && hive-configure.sh " + host; // provide IP address of server
	}

	public static String configureClient(Agent server) {
		String uri = "thrift://" + server.getListIP().get(0) + ":10000";
		return Commands.addHivePoperty("add", "hive-site.xml",
				"hive.metastore.uris", uri);
	}

	public static String addHivePoperty(String cmd, String propFile, String property, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(". /etc/profile && hive-property.sh ").append(cmd).append(" ");
		sb.append(propFile).append(" ").append(property);
		if (value != null) sb.append(" ").append(value);

		return sb.toString();
	}

}

package org.safehaus.subutai.impl.flume;

public class Commands {

	public static final String PACKAGE_NAME = "ksks-flume";

	public static String make(CommandType type) {
		switch (type) {
			case STATUS:
				return "dpkg -l | grep '^ii' | grep ksks";
			case INSTALL:
			case PURGE:
				StringBuilder sb = new StringBuilder();
				sb.append("apt-get --force-yes --assume-yes ");
				sb.append(type.toString().toLowerCase()).append(" ");
				sb.append(PACKAGE_NAME);
				return sb.toString();
			case START:
			case STOP:
				String s = "service flume-ng " + type.toString().toLowerCase() + " agent";
				if (type == CommandType.START) s += " &"; // TODO:
				return s;
			default:
				throw new AssertionError(type.name());

		}
	}

}

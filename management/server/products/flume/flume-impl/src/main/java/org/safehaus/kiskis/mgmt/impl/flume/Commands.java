package org.safehaus.kiskis.mgmt.impl.flume;

public class Commands {

    public static final String PACKAGE_NAME = "ksks-flume";

    public static String make(CommandType type) {
        switch(type) {
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
                return "service flume-ng " + type.toString().toLowerCase() + " agent";
            default:
                throw new AssertionError(type.name());

        }
    }

}

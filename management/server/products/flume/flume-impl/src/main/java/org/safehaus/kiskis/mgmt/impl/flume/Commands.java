package org.safehaus.kiskis.mgmt.impl.flume;

public class Commands {

    public static String make(CommandType type) {
        switch(type) {
            case STATUS:
                return "dpkg -l | grep '^ii' | grep ksks";
            case INSTALL:
                return "apt-get --force-yes --assume-yes install ksks-flume";
            case UNINSTALL:
                return "apt-get --force-yes --assume-yes purge ksks-flume";
            case START:
                return "service flume-ng start agent";
            case STOP:
                return "service flume-ng stop agent";
            default:
                throw new AssertionError(type.name());

        }
    }

}

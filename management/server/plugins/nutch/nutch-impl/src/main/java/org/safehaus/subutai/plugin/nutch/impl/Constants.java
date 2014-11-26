package org.safehaus.subutai.plugin.nutch.impl;


import org.safehaus.subutai.common.settings.Common;


public class Constants
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + "nutch";
    public static final String INSTALL = "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    public static final String UNINSTALL = "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    public static final String CHECK = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX;
}

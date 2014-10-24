package org.safehaus.subutai.plugin.hive.impl;


import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;


public enum Product
{

    HIVE( Common.PACKAGE_PREFIX + HiveConfig.PRODUCT_KEY.toLowerCase(), "hive-thrift", true ),
    DERBY( Common.PACKAGE_PREFIX + "derby", "derby", false );

    private final String packageName, serviceName;
    private final boolean profileScriptRun;


    private Product( String packageName, String serviceName, boolean profileScriptRun )
    {
        this.packageName = packageName;
        this.serviceName = serviceName;
        this.profileScriptRun = profileScriptRun;
    }


    public String getPackageName()
    {
        return packageName;
    }


    public String getServiceName()
    {
        return serviceName;
    }


    public boolean isProfileScriptRun()
    {
        return profileScriptRun;
    }


    @Override
    public String toString()
    {
        String s = super.toString();
        return s.substring( 0, 1 ).toUpperCase() + s.substring( 1 ).toLowerCase();
    }

}

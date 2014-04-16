package org.safehaus.kiskis.mgmt.impl.hive;

public enum Product {

    HIVE("ksks-hive", "hive-thrift"),
    DERBY("ksks-derby", "derby");

    private final String packageName, serviceName;

    private Product(String packageName, String serviceName) {
        this.packageName = packageName;
        this.serviceName = serviceName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getServiceName() {
        return serviceName;
    }

}

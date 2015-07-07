package io.subutai.common.metric;


public enum MetricType {

    RAM("ram"),
    CPU("cpu"),
    DISK_VAR("diskVar"),
    DISK_OPT("diskOpt"),
    DISK_ROOTFS("diskRootfs"),
    DISK_HOME("diskHome");

    private String name;

    MetricType( final String name ) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

}

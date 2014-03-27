/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management;

/**
 *
 * @author bahadyr
 */
public enum HBaseCommandEnum {
    //. /etc/profile &&
    START("service hbase start > /dev/null ", 10), // > /dev/null &
    STOP("service hbase stop > /dev/null ", 10),
    STATUS("service hbase status", 120),
    INSTALL("apt-get --assume-yes --force-yes install ksks-hbase", 300),
    SET_MASTER(". /etc/profile && $HBASE_HOME/scripts/master.sh", 300),
    SET_REGION(". /etc/profile && $HBASE_HOME/scripts/region.sh", 300),
    SET_QUORUM(". /etc/profile && $HBASE_HOME/scripts/quorum.sh", 300),
    SET_BACKUP_MASTERS(". /etc/profile && $HBASE_HOME/scripts/backUpMasters.sh", 300),
    PURGE("apt-get --assume-yes --force-yes purge ksks-hbase", 300),
    MANAGE("manage", 120);

    String program;
    int timeout;

    private HBaseCommandEnum(String program, int timeout) {
        this.program = program;
        this.timeout = timeout;
    }

    public String getProgram() {
        return program;
    }

}

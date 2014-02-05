/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management;

/**
 *
 * @author bahadyr
 */
public enum OozieCommandEnum {

    START("service hbase start", 120),
    STOP("service hbase stop", 120),
    STATUS("service hbase status", 120),
    INSTALL("apt-get --assume-yes --force-yes install ksks-hbase", 120),
    SET_MASTER("sh /opt/hbase-0.94.11/scripts/master.sh", 120),
    SET_REGION("sh /opt/hbase-0.94.11/scripts/region.sh", 120),
    SET_QUORUM("sh /opt/hbase-0.94.11/scripts/quorum.sh", 120),
    SET_BACKUP_MASTERS("sh /opt/hbase-0.94.11/scripts/backUpMasters.sh", 120),
    PURGE("apt-get --assume-yes --force-yes purge ksks-hbase", 120);

    String program;
    String yaml = "/opt/cassandra-2.0.3/conf/cassandra.yaml";
    int timeout;

    private OozieCommandEnum(String program, int timeout) {
        this.program = program;
        this.timeout = timeout;
    }

    public String getProgram() {
        return program;
    }

}

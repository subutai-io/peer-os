/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

/**
 *
 * @author bahadyr
 */
public enum CassandraCommandEnum {

    START("service cassandra start", 180),
    STOP("service cassandra stop", 180),
    STATUS("service cassandra status", 180),
    INSTALL("apt-get --assume-yes --force-yes install ksks-cassandra", 180),
    SET_SEED("", 180),
    REMOVE_SEED("", 180),
    PURGE("apt-get --assume-yes --force-yes purge ksks-cassandra", 180);

    String program;
    String yaml = "$CASSANDRA_HOME/conf/cassandra.yaml";
    int timeout;

    private CassandraCommandEnum(String program, int timeout) {
        this.program = program;
        this.timeout = timeout;
    }

    public String getProgram() {
        return program;
    }

}

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

    START("service cassandra start", 30),
    STOP("service cassandra stop", 30),
    STATUS("service cassandra status", 30),
    INSTALL("apt-get --assume-yes --force-yes install ksks-cassandra", 30),
    SET_SEED("", 30),
    REMOVE_SEED("", 30),
    PURGE("apt-get --assume-yes --force-yes purge ksks-cassandra", 30);

    String program;
    String yaml = "/opt/cassandra-2.0.3/conf/cassandra.yaml";
    int timeout;

    private CassandraCommandEnum(String program, int timeout) {
        this.program = program;
        this.timeout = timeout;
    }

    public String getProgram() {
        return program;
    }

}
